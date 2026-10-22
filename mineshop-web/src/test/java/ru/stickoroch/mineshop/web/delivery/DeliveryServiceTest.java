package ru.stickoroch.mineshop.web.delivery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import ru.stickoroch.mineshop.core.DeliveryStatus;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.PlatformType;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.OrderDeliveryModel;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.core.model.UserModel;
import ru.stickoroch.mineshop.core.repository.DeliveryQueueRepository;
import ru.stickoroch.mineshop.core.repository.OrderRepository;
import ru.stickoroch.mineshop.web.plugin.PluginMessage;
import ru.stickoroch.mineshop.web.plugin.PluginProperties;
import ru.stickoroch.mineshop.web.plugin.PluginSession;
import ru.stickoroch.mineshop.web.plugin.PluginSessionRegistry;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeliveryServiceTest {

    private final DeliveryQueueRepository deliveryQueueRepository = mock(DeliveryQueueRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final PluginSessionRegistry sessions = mock(PluginSessionRegistry.class);
    private final Logger logger = mock(Logger.class);
    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

    private final PluginProperties properties = new PluginProperties();
    private final PluginSession pluginSession = mock(PluginSession.class);

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        properties.setMaxAttempts(3);
        properties.setStaleInProgressMinutes(2);

        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

        deliveryService = new DeliveryService(
                deliveryQueueRepository,
                orderRepository,
                sessions,
                properties,
                logger,
                transactionManager
        );
    }

    @Test
    void enqueueStoresPendingDeliveryWithSnapshots() {
        OrderModel order = order();

        deliveryService.enqueue(order);

        ArgumentCaptor<OrderDeliveryModel> captor = ArgumentCaptor.forClass(OrderDeliveryModel.class);
        verify(deliveryQueueRepository).save(captor.capture());

        OrderDeliveryModel delivery = captor.getValue();
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(delivery.getPlayerName()).isEqualTo("Steve");
        assertThat(delivery.getCommand()).isEqualTo("give %player% diamond 1");
        assertThat(delivery.getOrder()).isSameAs(order);
    }

    @Test
    void enqueueMarksOrderForManualDeliveryWhenNicknameIsMissing() {
        OrderModel order = order();
        order.getUser().setMcNickname("-");

        deliveryService.enqueue(order);

        ArgumentCaptor<OrderDeliveryModel> captor = ArgumentCaptor.forClass(OrderDeliveryModel.class);
        verify(deliveryQueueRepository).save(captor.capture());

        assertThat(captor.getValue().getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(captor.getValue().getLastError()).contains("ник");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_DELIVERY);
        verify(orderRepository).save(order);
    }

    @Test
    void dispatchSendsDeliverMessageAndMarksInProgress() throws Exception {
        OrderModel order = order();
        OrderDeliveryModel delivery = delivery(order);

        when(sessions.getActive()).thenReturn(Optional.of(pluginSession));
        when(deliveryQueueRepository.findByOrder_PaymentId(order.getPaymentId()))
                .thenReturn(Optional.of(delivery));

        deliveryService.dispatchNewOrder(order.getPaymentId());

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_PROGRESS);
        assertThat(delivery.getAttempts()).isEqualTo(1);

        ArgumentCaptor<PluginMessage> captor = ArgumentCaptor.forClass(PluginMessage.class);
        verify(pluginSession).send(captor.capture());

        PluginMessage message = captor.getValue();
        assertThat(message.type()).isEqualTo(PluginMessage.TYPE_DELIVER);
        assertThat(message.orderId()).isEqualTo(order.getPaymentId());
        assertThat(message.player()).isEqualTo("Steve");
        assertThat(message.amount()).isEqualTo(1);
    }

    @Test
    void dispatchKeepsOrderInQueueWhenPluginIsOffline() throws Exception {
        OrderModel order = order();
        OrderDeliveryModel delivery = delivery(order);

        when(sessions.getActive()).thenReturn(Optional.empty());
        when(deliveryQueueRepository.findByOrder_PaymentId(order.getPaymentId()))
                .thenReturn(Optional.of(delivery));

        deliveryService.dispatchNewOrder(order.getPaymentId());

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        verify(pluginSession, never()).send(any());
    }

    @Test
    void successfulResultMarksOrderDelivered() {
        OrderModel order = order();
        OrderDeliveryModel delivery = delivery(order);
        delivery.setStatus(DeliveryStatus.IN_PROGRESS);
        delivery.setAttempts(1);

        when(deliveryQueueRepository.findByOrder_PaymentId(order.getPaymentId()))
                .thenReturn(Optional.of(delivery));

        deliveryService.handleResult(order.getPaymentId(), true, null);

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getDeliveredAt()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getCompleteAt()).isNotNull();
    }

    @Test
    void offlinePlayerReturnsOrderToQueue() {
        OrderModel order = order();
        OrderDeliveryModel delivery = delivery(order);
        delivery.setStatus(DeliveryStatus.IN_PROGRESS);
        delivery.setAttempts(1);

        when(deliveryQueueRepository.findByOrder_PaymentId(order.getPaymentId()))
                .thenReturn(Optional.of(delivery));

        deliveryService.handleResult(order.getPaymentId(), false, "PLAYER_OFFLINE");

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
        assertThat(delivery.getLastError()).isEqualTo("PLAYER_OFFLINE");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void exhaustedAttemptsRequireManualDelivery() {
        OrderModel order = order();
        OrderDeliveryModel delivery = delivery(order);
        delivery.setStatus(DeliveryStatus.IN_PROGRESS);
        delivery.setAttempts(3);

        when(deliveryQueueRepository.findByOrder_PaymentId(order.getPaymentId()))
                .thenReturn(Optional.of(delivery));

        deliveryService.handleResult(order.getPaymentId(), false, "COMMAND_FAILED");

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_DELIVERY);
    }

    @Test
    void syncClaimsAllPendingAndSendsList() throws Exception {
        OrderModel first = order();
        OrderModel second = order();
        OrderDeliveryModel firstDelivery = delivery(first);
        OrderDeliveryModel secondDelivery = delivery(second);

        when(deliveryQueueRepository.findAllByStatusOrderByCreatedAtAsc(DeliveryStatus.IN_PROGRESS))
                .thenReturn(List.of());
        when(deliveryQueueRepository.findAllByStatusOrderByCreatedAtAsc(DeliveryStatus.PENDING))
                .thenReturn(List.of(firstDelivery, secondDelivery));

        deliveryService.sendPending(pluginSession);

        assertThat(firstDelivery.getStatus()).isEqualTo(DeliveryStatus.IN_PROGRESS);
        assertThat(secondDelivery.getStatus()).isEqualTo(DeliveryStatus.IN_PROGRESS);

        ArgumentCaptor<PluginMessage> captor = ArgumentCaptor.forClass(PluginMessage.class);
        verify(pluginSession).send(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(PluginMessage.TYPE_PENDING);
        assertThat(captor.getValue().orders()).hasSize(2);
    }

    @Test
    void playerOnlineSendsOnlyHisOrders() throws Exception {
        OrderModel order = order();
        OrderDeliveryModel delivery = delivery(order);

        when(deliveryQueueRepository.findAllByPlayerNameIgnoreCaseAndStatusOrderByCreatedAtAsc(
                "Steve", DeliveryStatus.PENDING)).thenReturn(List.of(delivery));

        deliveryService.sendPendingForPlayer(pluginSession, " Steve ");

        ArgumentCaptor<PluginMessage> captor = ArgumentCaptor.forClass(PluginMessage.class);
        verify(pluginSession).send(captor.capture());
        assertThat(captor.getValue().orders()).hasSize(1);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.IN_PROGRESS);
    }

    @Test
    void disconnectReleasesInProgressTasks() {
        OrderModel order = order();
        OrderDeliveryModel delivery = delivery(order);
        delivery.setStatus(DeliveryStatus.IN_PROGRESS);

        when(deliveryQueueRepository.findAllByStatusOrderByCreatedAtAsc(DeliveryStatus.IN_PROGRESS))
                .thenReturn(List.of(delivery));

        deliveryService.releaseInProgress();

        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PENDING);
    }

    private OrderModel order() {
        UserModel user = UserModel.builder()
                .id(new PlatformUserId(1L, PlatformType.TELEGRAM))
                .mcNickname("Steve")
                .build();

        ProductModel product = ProductModel.builder()
                .id(5L)
                .title("Алмазный меч")
                .price(new BigDecimal("100.00"))
                .giveCommand("give %player% diamond 1")
                .isActive(true)
                .build();

        return OrderModel.builder()
                .paymentId(UUID.randomUUID())
                .user(user)
                .product(product)
                .amount(1)
                .cost(product.getPrice())
                .status(OrderStatus.PAID)
                .paymentLink("https://yookassa.test/pay")
                .build();
    }

    private OrderDeliveryModel delivery(OrderModel order) {
        return OrderDeliveryModel.builder()
                .order(order)
                .playerName("Steve")
                .command(order.getProduct().getGiveCommand())
                .status(DeliveryStatus.PENDING)
                .attempts(0)
                .build();
    }
}
