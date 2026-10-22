package ru.stickoroch.mineshop.web.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import ru.deelter.yookassa.YooKassa;
import ru.deelter.yookassa.data.impl.Payment;
import ru.deelter.yookassa.data.impl.PaymentStatus;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.PlatformType;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.core.model.UserModel;
import ru.stickoroch.mineshop.core.repository.OrderRepository;
import ru.stickoroch.mineshop.core.repository.ProductRepository;
import ru.stickoroch.mineshop.core.repository.UserRepository;
import ru.stickoroch.mineshop.web.delivery.DeliveryService;
import ru.stickoroch.mineshop.web.delivery.OrderPaidEvent;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final YooKassa yooKassa = mock(YooKassa.class);
    private final DeliveryService deliveryService = mock(DeliveryService.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final Logger logger = mock(Logger.class);

    private final PlatformUserId userId = new PlatformUserId(1L, PlatformType.TELEGRAM);

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                userRepository,
                productRepository,
                yooKassa,
                deliveryService,
                eventPublisher,
                logger,
                "https://t.me"
        );
    }

    @Test
    void createsPendingOrderWithYooKassaPaymentLink() throws Exception {
        UserModel user = UserModel.builder().id(userId).mcNickname("Steve").build();
        ProductModel product = ProductModel.builder()
                .id(5L)
                .title("Алмазный меч")
                .price(new BigDecimal("199.00"))
                .isActive(true)
                .build();

        UUID paymentId = UUID.randomUUID();
        Payment payment = mock(Payment.class);
        when(payment.getId()).thenReturn(paymentId);
        when(payment.getConfirmation()).thenReturn(new Payment.ConfirmationType("https://yookassa.test/pay"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(yooKassa.createPayment(any())).thenReturn(payment);
        when(orderRepository.save(any(OrderModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderModel order = orderService.createOrder(userId, 5L);

        assertThat(order.getPaymentId()).isEqualTo(paymentId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getCost()).isEqualByComparingTo("199.00");
        assertThat(order.getAmount()).isEqualTo(1);
        assertThat(order.getPaymentLink()).isEqualTo("https://yookassa.test/pay");
        assertThat(order.getUser()).isSameAs(user);
        assertThat(order.getProduct()).isSameAs(product);
    }

    @Test
    void confirmsPaidOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderModel order = pendingOrder(orderId);

        Payment payment = mock(Payment.class);
        when(payment.isPaid()).thenReturn(true);
        when(yooKassa.getPayment(orderId)).thenReturn(payment);
        when(orderRepository.findByPaymentIdAndUser_Id(orderId, userId)).thenReturn(Optional.of(order));

        OrderService.PaymentConfirmationResult result = orderService.confirmPayment(orderId, userId);

        assertThat(result).isEqualTo(OrderService.PaymentConfirmationResult.PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidAt()).isNotNull();
        verify(deliveryService).enqueue(order);
        verify(eventPublisher).publishEvent(any(OrderPaidEvent.class));
    }

    @Test
    void keepsOrderPendingWhenPaymentNotFound() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderModel order = pendingOrder(orderId);

        Payment payment = mock(Payment.class);
        when(payment.isPaid()).thenReturn(false);
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(yooKassa.getPayment(orderId)).thenReturn(payment);
        when(orderRepository.findByPaymentIdAndUser_Id(orderId, userId)).thenReturn(Optional.of(order));

        OrderService.PaymentConfirmationResult result = orderService.confirmPayment(orderId, userId);

        assertThat(result).isEqualTo(OrderService.PaymentConfirmationResult.NOT_PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void cancelsPendingOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderModel order = pendingOrder(orderId);

        Payment payment = mock(Payment.class);
        when(payment.isPaid()).thenReturn(false);
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDING);
        when(yooKassa.getPayment(orderId)).thenReturn(payment);
        when(orderRepository.findByPaymentIdAndUser_Id(orderId, userId)).thenReturn(Optional.of(order));

        OrderService.OrderCancellationResult result = orderService.cancelOrder(orderId, userId);

        assertThat(result).isEqualTo(OrderService.OrderCancellationResult.CANCELED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getCompleteAt()).isNotNull();
        verify(yooKassa).cancelPayment(orderId);
    }

    @Test
    void doesNotCancelAlreadyPaidOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderModel order = pendingOrder(orderId);

        Payment payment = mock(Payment.class);
        when(payment.isPaid()).thenReturn(true);
        when(yooKassa.getPayment(orderId)).thenReturn(payment);
        when(orderRepository.findByPaymentIdAndUser_Id(orderId, userId)).thenReturn(Optional.of(order));

        OrderService.OrderCancellationResult result = orderService.cancelOrder(orderId, userId);

        assertThat(result).isEqualTo(OrderService.OrderCancellationResult.PAYMENT_ALREADY_PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void doesNotCancelWhenPaymentCannotBeVerified() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderModel order = pendingOrder(orderId);

        when(orderRepository.findByPaymentIdAndUser_Id(orderId, userId)).thenReturn(Optional.of(order));
        when(yooKassa.getPayment(orderId)).thenThrow(new IOException("YooKassa is unavailable"));

        OrderService.OrderCancellationResult result = orderService.cancelOrder(orderId, userId);

        assertThat(result).isEqualTo(OrderService.OrderCancellationResult.ERROR);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(yooKassa, never()).cancelPayment(orderId);
    }

    private OrderModel pendingOrder(UUID orderId) {
        return OrderModel.builder()
                .paymentId(orderId)
                .status(OrderStatus.PENDING)
                .paymentLink("https://yookassa.test/pay")
                .build();
    }
}
