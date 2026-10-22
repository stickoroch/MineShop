package ru.stickoroch.mineshop.web.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.deelter.yookassa.YooKassa;
import ru.deelter.yookassa.data.impl.Amount;
import ru.deelter.yookassa.data.impl.Currency;
import ru.deelter.yookassa.data.impl.Payment;
import ru.deelter.yookassa.data.impl.PaymentStatus;
import ru.deelter.yookassa.data.impl.requests.PaymentCreateData;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.core.model.UserModel;
import ru.stickoroch.mineshop.core.repository.OrderRepository;
import ru.stickoroch.mineshop.core.repository.ProductRepository;
import ru.stickoroch.mineshop.core.repository.UserRepository;
import ru.stickoroch.mineshop.web.delivery.DeliveryService;
import ru.stickoroch.mineshop.web.delivery.OrderPaidEvent;
import ru.stickoroch.mineshop.web.dto.Dtos;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {

    public static final int PAGE_SIZE = 10;
    public static final List<OrderStatus> HISTORY_STATUSES = List.of(
            OrderStatus.PAID,
            OrderStatus.DELIVERED,
            OrderStatus.PENDING_DELIVERY,
            OrderStatus.CANCELED
    );
    public static final List<OrderStatus> ACTIVE_STATUSES = List.of(OrderStatus.PENDING);

    private static final int MAX_PAYMENT_DESCRIPTION_LENGTH = 128;

    OrderRepository orderRepository;
    UserRepository userRepository;
    ProductRepository productRepository;
    YooKassa yooKassa;
    DeliveryService deliveryService;
    ApplicationEventPublisher eventPublisher;
    Logger logger;
    String returnUrl;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        YooKassa yooKassa,
                        DeliveryService deliveryService,
                        ApplicationEventPublisher eventPublisher,
                        Logger logger,
                        @Value("${app.yooKassa.return-url}") String returnUrl) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.yooKassa = yooKassa;
        this.deliveryService = deliveryService;
        this.eventPublisher = eventPublisher;
        this.logger = logger;
        this.returnUrl = returnUrl;
    }

    @Transactional(readOnly = true)
    public OrderModel getOrder(UUID id) {
        return orderRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Optional<OrderModel> findOrderForUser(UUID orderId, PlatformUserId userId) {
        return orderRepository.findByPaymentIdAndUser_Id(orderId, userId);
    }

    @Transactional(readOnly = true)
    public Page<OrderModel> getOrders(int page) {
        return orderRepository.findAll(pageRequest(page));
    }

    @Transactional(readOnly = true)
    public Page<OrderModel> getActiveOrders(int page) {
        return orderRepository.findAllByStatusIn(pageRequest(page), ACTIVE_STATUSES);
    }

    @Transactional(readOnly = true)
    public Page<OrderModel> getHistoryPage(PlatformUserId userId, int page) {
        return orderRepository.findAllByUser_IdAndStatusIn(userId, HISTORY_STATUSES, pageRequest(page));
    }

    @Transactional(readOnly = true)
    public Page<OrderModel> getActiveOrdersPage(PlatformUserId userId, int page) {
        return orderRepository.findAllByUser_IdAndStatusIn(userId, ACTIVE_STATUSES, pageRequest(page));
    }

    public OrderModel createOrder(PlatformUserId userId, long productId) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        ProductModel product = productRepository.findById(productId)
                .filter(model -> Boolean.TRUE.equals(model.getIsActive()))
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден или снят с продажи"));

        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("Некорректная цена товара");
        }

        Payment payment = createPayment(product);

        OrderModel orderModel = OrderModel.builder()
                .paymentId(payment.getId())
                .user(user)
                .product(product)
                .amount(1)
                .cost(product.getPrice())
                .status(OrderStatus.PENDING)
                .paymentLink(getPaymentLink(payment))
                .build();

        return orderRepository.save(orderModel);
    }

    public PaymentConfirmationResult confirmPayment(UUID orderId, PlatformUserId userId) {
        Optional<OrderModel> found = orderRepository.findByPaymentIdAndUser_Id(orderId, userId);

        if (found.isEmpty()) {
            return PaymentConfirmationResult.NOT_FOUND;
        }

        OrderModel order = found.get();

        if (order.getStatus() != OrderStatus.PENDING) {
            return PaymentConfirmationResult.ALREADY_PROCESSED;
        }

        try {
            Payment payment = yooKassa.getPayment(order.getPaymentId());

            if (payment != null && (payment.isPaid() || payment.getStatus() == PaymentStatus.SUCCEEDED)) {
                markPaid(order);
                return PaymentConfirmationResult.PAID;
            }

            if (payment != null && payment.getStatus() == PaymentStatus.CANCELED) {
                markCanceled(order);
                return PaymentConfirmationResult.PAYMENT_CANCELED;
            }

            return PaymentConfirmationResult.NOT_PAID;
        } catch (Exception e) {
            logger.warn("Could not verify YooKassa payment {}: {}", orderId, e.getMessage());
            return PaymentConfirmationResult.ERROR;
        }
    }

    public OrderCancellationResult cancelOrder(UUID orderId, PlatformUserId userId) {
        Optional<OrderModel> found = orderRepository.findByPaymentIdAndUser_Id(orderId, userId);

        if (found.isEmpty()) {
            return OrderCancellationResult.NOT_FOUND;
        }

        OrderModel order = found.get();

        if (order.getStatus() != OrderStatus.PENDING) {
            return OrderCancellationResult.ALREADY_PROCESSED;
        }

        try {
            Payment payment = yooKassa.getPayment(order.getPaymentId());

            if (payment != null && (payment.isPaid() || payment.getStatus() == PaymentStatus.SUCCEEDED)) {
                markPaid(order);
                return OrderCancellationResult.PAYMENT_ALREADY_PAID;
            }

            if (payment != null && payment.getStatus() == PaymentStatus.CANCELED) {
                markCanceled(order);
                return OrderCancellationResult.CANCELED;
            }
        } catch (Exception e) {
            logger.warn("Could not check YooKassa payment {} before cancel: {}", orderId, e.getMessage());
            return OrderCancellationResult.ERROR;
        }

        try {
            yooKassa.cancelPayment(order.getPaymentId());
        } catch (Exception e) {
            logger.warn("Could not cancel YooKassa payment {}: {}", orderId, e.getMessage());
            return OrderCancellationResult.ERROR;
        }

        markCanceled(order);

        return OrderCancellationResult.CANCELED;
    }

    public void setOrderStatus(Dtos.OrderSetStatus r) {
        OrderModel orderModel = orderRepository.findById(r.orderId()).orElseThrow();

        orderModel.setStatus(r.status());

        if (r.status() == OrderStatus.DELIVERED) {
            orderModel.setCompleteAt(Instant.now());
        }

        if (r.status() == OrderStatus.PAID && orderModel.getPaidAt() == null) {
            orderModel.setPaidAt(Instant.now());
        }

        orderRepository.save(orderModel);

        switch (r.status()) {
            case DELIVERED -> deliveryService.markDeliveredManually(orderModel.getPaymentId());
            case CANCELED -> deliveryService.markCanceled(orderModel.getPaymentId());
            case PAID -> {
                deliveryService.requeue(orderModel);
                eventPublisher.publishEvent(new OrderPaidEvent(orderModel.getPaymentId()));
            }
            default -> {
            }
        }
    }

    private Payment createPayment(ProductModel product) {
        try {
            PaymentCreateData data = PaymentCreateData.create(
                    new Amount(product.getPrice(), Currency.RUB),
                    returnUrl,
                    limitDescription("Заказ: " + product.getTitle())
            );

            return yooKassa.createPayment(data);
        } catch (Exception e) {
            throw new OrderCreationException("Не удалось создать платёж ЮKassa", e);
        }
    }

    private String getPaymentLink(Payment payment) {
        if (payment == null || payment.getId() == null) {
            throw new OrderCreationException("ЮKassa не вернула платёж", null);
        }

        String link = payment.getConfirmation() != null
                ? payment.getConfirmation().getUrl()
                : payment.getRedirectUrl();

        if (link == null || link.isBlank()) {
            throw new OrderCreationException("ЮKassa не вернула ссылку на оплату", null);
        }

        return link;
    }

    private void markPaid(OrderModel order) {
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(Instant.now());

        orderRepository.save(order);

        deliveryService.enqueue(order);
        eventPublisher.publishEvent(new OrderPaidEvent(order.getPaymentId()));
    }

    private void markCanceled(OrderModel order) {
        order.setStatus(OrderStatus.CANCELED);
        order.setCompleteAt(Instant.now());
        orderRepository.save(order);
    }

    private PageRequest pageRequest(int page) {
        return PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private String limitDescription(String description) {
        if (description.length() <= MAX_PAYMENT_DESCRIPTION_LENGTH) {
            return description;
        }

        return description.substring(0, MAX_PAYMENT_DESCRIPTION_LENGTH - 1) + "…";
    }

    public enum PaymentConfirmationResult {
        PAID,
        NOT_PAID,
        PAYMENT_CANCELED,
        NOT_FOUND,
        ALREADY_PROCESSED,
        ERROR
    }

    public enum OrderCancellationResult {
        CANCELED,
        NOT_FOUND,
        ALREADY_PROCESSED,
        PAYMENT_ALREADY_PAID,
        ERROR
    }

    public static class OrderCreationException extends RuntimeException {
        public OrderCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
