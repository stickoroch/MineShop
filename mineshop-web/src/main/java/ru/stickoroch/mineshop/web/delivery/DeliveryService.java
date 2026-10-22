package ru.stickoroch.mineshop.web.delivery;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.stickoroch.mineshop.core.DeliveryStatus;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.model.OrderDeliveryModel;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.repository.DeliveryQueueRepository;
import ru.stickoroch.mineshop.core.repository.OrderRepository;
import ru.stickoroch.mineshop.web.plugin.DeliveryTask;
import ru.stickoroch.mineshop.web.plugin.PluginMessage;
import ru.stickoroch.mineshop.web.plugin.PluginProperties;
import ru.stickoroch.mineshop.web.plugin.PluginSession;
import ru.stickoroch.mineshop.web.plugin.PluginSessionRegistry;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Доставка оплаченных заказов на Minecraft-сервер.
 *
 * Порядок работы:
 * 1. при оплате заказ кладётся в очередь ({@code order_deliveries});
 * 2. если плагин подключён — задача сразу отправляется ему;
 * 3. плагин отвечает {@code delivery_result};
 * 4. при ошибке (например, игрок офлайн) задача возвращается в PENDING
 *    и будет отправлена снова при входе игрока или переподключении плагина.
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeliveryService {

    private static final int MAX_ERROR_LENGTH = 255;

    DeliveryQueueRepository deliveryQueueRepository;
    OrderRepository orderRepository;
    PluginSessionRegistry sessions;
    PluginProperties properties;
    Logger logger;
    TransactionTemplate transactionTemplate;

    public DeliveryService(DeliveryQueueRepository deliveryQueueRepository,
                           OrderRepository orderRepository,
                           PluginSessionRegistry sessions,
                           PluginProperties properties,
                           Logger logger,
                           PlatformTransactionManager transactionManager) {
        this.deliveryQueueRepository = deliveryQueueRepository;
        this.orderRepository = orderRepository;
        this.sessions = sessions;
        this.properties = properties;
        this.logger = logger;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Создаёт задачу выдачи. Вызывается в транзакции оплаты заказа,
     * поэтому запись в очередь появляется атомарно вместе со статусом PAID.
     */
    @Transactional
    public void enqueue(OrderModel order) {
        if (deliveryQueueRepository.findByOrder_PaymentId(order.getPaymentId()).isPresent()) {
            return;
        }

        String playerName = order.getUser() != null ? normalizePlayer(order.getUser().getMcNickname()) : null;
        String command = order.getProduct() != null ? order.getProduct().getGiveCommand() : null;

        OrderDeliveryModel delivery = OrderDeliveryModel.builder()
                .order(order)
                .playerName(playerName == null ? "-" : playerName)
                .command(command == null ? "" : command)
                .status(DeliveryStatus.PENDING)
                .attempts(0)
                .build();

        if (playerName == null || command == null || command.isBlank()) {
            delivery.setStatus(DeliveryStatus.FAILED);
            delivery.setLastError(playerName == null
                    ? "У пользователя не указан ник Minecraft"
                    : "У товара не настроена команда выдачи");

            order.setStatus(OrderStatus.PENDING_DELIVERY);
            orderRepository.save(order);
        }

        deliveryQueueRepository.save(delivery);
    }

    /**
     * Пытается отправить только что оплаченный заказ плагину.
     * Вызывается после коммита транзакции оплаты.
     */
    public void dispatchNewOrder(UUID orderId) {
        Optional<PluginSession> active = sessions.getActive();

        if (active.isEmpty()) {
            logger.info("Plugin is offline, order {} is waiting in delivery queue", orderId);
            return;
        }

        DeliveryTask task = claim(orderId);

        if (task == null) {
            return;
        }

        try {
            active.get().send(PluginMessage.deliver(task));
        } catch (Exception e) {
            logger.warn("Could not send order {} to plugin: {}", orderId, e.getMessage());
            release(List.of(orderId));
        }
    }

    /**
     * Плагин подключился и просит все невыданные заказы.
     * Все «зависшие» IN_PROGRESS сбрасываются: их сессия уже потеряна.
     */
    public void sendPending(PluginSession session) {
        List<DeliveryTask> tasks = transactionTemplate.execute(status -> {
            deliveryQueueRepository.findAllByStatusOrderByCreatedAtAsc(DeliveryStatus.IN_PROGRESS)
                    .forEach(delivery -> {
                        delivery.setStatus(DeliveryStatus.PENDING);
                        delivery.setLastError("Соединение с плагином было потеряно");
                    });

            return deliveryQueueRepository.findAllByStatusOrderByCreatedAtAsc(DeliveryStatus.PENDING)
                    .stream()
                    .map(this::markInProgress)
                    .toList();
        });

        sendTasks(session, tasks);
    }

    /**
     * Игрок зашёл на сервер — отправляем только его невыданные заказы.
     */
    public void sendPendingForPlayer(PluginSession session, String playerName) {
        String player = normalizePlayer(playerName);

        if (player == null) {
            return;
        }

        List<DeliveryTask> tasks = transactionTemplate.execute(status ->
                deliveryQueueRepository
                        .findAllByPlayerNameIgnoreCaseAndStatusOrderByCreatedAtAsc(player, DeliveryStatus.PENDING)
                        .stream()
                        .map(this::markInProgress)
                        .toList()
        );

        sendTasks(session, tasks);
    }

    /**
     * Результат выполнения команды плагином.
     */
    public void handleResult(UUID orderId, boolean success, String error) {
        transactionTemplate.executeWithoutResult(status -> {
            OrderDeliveryModel delivery = deliveryQueueRepository.findByOrder_PaymentId(orderId).orElse(null);

            if (delivery == null || delivery.getStatus() == DeliveryStatus.DELIVERED) {
                return;
            }

            OrderModel order = delivery.getOrder();

            if (success) {
                delivery.setStatus(DeliveryStatus.DELIVERED);
                delivery.setDeliveredAt(Instant.now());
                delivery.setLastError(null);

                order.setStatus(OrderStatus.DELIVERED);
                order.setCompleteAt(Instant.now());
                orderRepository.save(order);

                logger.info("Order {} delivered to player {}", orderId, delivery.getPlayerName());
                return;
            }

            delivery.setLastError(limitError(error));

            if (delivery.getAttempts() >= properties.getMaxAttempts()) {
                delivery.setStatus(DeliveryStatus.FAILED);
                order.setStatus(OrderStatus.PENDING_DELIVERY);
                orderRepository.save(order);

                logger.warn(
                        "Order {} delivery failed after {} attempts ({}), manual delivery required",
                        orderId,
                        delivery.getAttempts(),
                        error
                );
            } else {
                delivery.setStatus(DeliveryStatus.PENDING);

                logger.info(
                        "Order {} delivery postponed (attempt {}): {}",
                        orderId,
                        delivery.getAttempts(),
                        error
                );
            }
        });
    }

    /**
     * Плагин отключился: его незавершённые задачи возвращаются в очередь.
     */
    @Transactional
    public void releaseInProgress() {
        deliveryQueueRepository.findAllByStatusOrderByCreatedAtAsc(DeliveryStatus.IN_PROGRESS)
                .forEach(delivery -> {
                    delivery.setStatus(DeliveryStatus.PENDING);
                    delivery.setLastError("Соединение с плагином было потеряно");
                });
    }

    /**
     * Заказ выдан вручную администратором — снимаем его с автоматической выдачи.
     */
    @Transactional
    public void markDeliveredManually(UUID orderId) {
        deliveryQueueRepository.findByOrder_PaymentId(orderId).ifPresent(delivery -> {
            delivery.setStatus(DeliveryStatus.DELIVERED);
            delivery.setDeliveredAt(Instant.now());
            delivery.setLastError(null);
        });
    }

    /**
     * Заказ отменён администратором — больше не пытаемся его выдать.
     */
    @Transactional
    public void markCanceled(UUID orderId) {
        deliveryQueueRepository.findByOrder_PaymentId(orderId).ifPresent(delivery -> {
            delivery.setStatus(DeliveryStatus.FAILED);
            delivery.setLastError("Заказ отменён администратором");
        });
    }

    /**
     * Возвращает заказ в очередь (например, админ вручную перевёл его в PAID).
     */
    @Transactional
    public void requeue(OrderModel order) {
        OrderDeliveryModel existing = deliveryQueueRepository
                .findByOrder_PaymentId(order.getPaymentId())
                .orElse(null);

        if (existing == null) {
            enqueue(order);
            return;
        }

        existing.setStatus(DeliveryStatus.PENDING);
        existing.setAttempts(0);
        existing.setLastError(null);
    }

    /**
     * Страховка от падения бэкенда в момент выдачи.
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void resetStaleInProgress() {
        Instant cutoff = Instant.now().minus(properties.getStaleInProgressMinutes(), ChronoUnit.MINUTES);

        List<OrderDeliveryModel> stale = deliveryQueueRepository
                .findAllByStatusAndUpdatedAtBefore(DeliveryStatus.IN_PROGRESS, cutoff);

        if (!stale.isEmpty()) {
            stale.forEach(delivery -> {
                delivery.setStatus(DeliveryStatus.PENDING);
                delivery.setLastError("Попытка выдачи зависла и была возвращена в очередь");
            });

            logger.warn("Returned {} stale delivery attempts to the queue", stale.size());
        }
    }

    private void sendTasks(PluginSession session, List<DeliveryTask> tasks) {
        if (tasks == null) {
            return;
        }

        try {
            session.send(PluginMessage.pending(tasks));
        } catch (Exception e) {
            logger.warn("Could not send pending deliveries to plugin: {}", e.getMessage());
            release(tasks.stream().map(DeliveryTask::orderId).toList());
        }
    }

    private DeliveryTask claim(UUID orderId) {
        return transactionTemplate.execute(status -> deliveryQueueRepository
                .findByOrder_PaymentId(orderId)
                .filter(delivery -> delivery.getStatus() == DeliveryStatus.PENDING)
                .map(this::markInProgress)
                .orElse(null));
    }

    private DeliveryTask markInProgress(OrderDeliveryModel delivery) {
        delivery.setStatus(DeliveryStatus.IN_PROGRESS);
        delivery.setAttempts(delivery.getAttempts() + 1);

        OrderModel order = delivery.getOrder();

        return new DeliveryTask(
                order.getPaymentId(),
                delivery.getPlayerName(),
                delivery.getCommand(),
                order.getAmount()
        );
    }

    private void release(Collection<UUID> orderIds) {
        if (orderIds.isEmpty()) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> orderIds.forEach(orderId ->
                deliveryQueueRepository.findByOrder_PaymentId(orderId)
                        .filter(delivery -> delivery.getStatus() == DeliveryStatus.IN_PROGRESS)
                        .ifPresent(delivery -> delivery.setStatus(DeliveryStatus.PENDING))
        ));
    }

    private String normalizePlayer(String playerName) {
        if (playerName == null) {
            return null;
        }

        String normalized = playerName.trim();

        if (normalized.isEmpty() || "-".equals(normalized)) {
            return null;
        }

        return normalized;
    }

    private String limitError(String error) {
        String message = error == null || error.isBlank() ? "Неизвестная ошибка плагина" : error;

        if (message.length() <= MAX_ERROR_LENGTH) {
            return message;
        }

        return message.substring(0, MAX_ERROR_LENGTH - 1) + "…";
    }
}
