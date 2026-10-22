package ru.stickoroch.mineshop.bot.routing.routes.callback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;
import ru.stickoroch.mineshop.bot.update.UpdateType;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.PlatformType;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.web.service.OrderService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderFlowTest {

    private final BotContext context = mock(BotContext.class);
    private final OrderService orderService = mock(OrderService.class);
    private final BotSessionService sessions = mock(BotSessionService.class);
    private final BotFormSender sender = mock(BotFormSender.class);
    private final LocalizationService localizationService = mock(LocalizationService.class);
    private final Logger logger = mock(Logger.class);

    private final PlatformUserId userId = new PlatformUserId(1L, PlatformType.TELEGRAM);
    private BotSessionModel session;

    @BeforeEach
    void setUp() {
        session = new BotSessionModel();
        session.setUserId(userId);
        session.setState(BotState.MENU);

        when(localizationService.getText(any(LocalizationKey.class))).thenReturn("text");

        doAnswer(invocation -> {
            Consumer<BotSessionModel> modifier = invocation.getArgument(1);
            modifier.accept(session);
            return session;
        }).when(sessions).update(eq(userId), any());
    }

    @Test
    void buyingProductCreatesOrderAndShowsOrderPage() {
        OrderModel order = order(OrderStatus.PENDING);
        when(orderService.createOrder(any(), eq(5L))).thenReturn(order);

        BuyProductCallback callback = new BuyProductCallback(
                orderService, sessions, sender, localizationService, logger);

        callback.handle(callbackUpdate("buy:5"), session);

        assertThat(session.getState()).isEqualTo(BotState.ORDER);
        assertThat(session.getProductId()).isEqualTo(5L);
        verify(sender).sendOrderMessage(userId, order);
    }

    @Test
    void historyCallbackShowsHistory() {
        HistoryCallback callback = new HistoryCallback(sessions, sender);

        callback.handle(callbackUpdate("history"), session);

        assertThat(session.getState()).isEqualTo(BotState.HISTORY);
        verify(sender).sendHistoryMenu(userId, 0);
    }

    @Test
    void activeOrdersCallbackShowsActiveOrders() {
        HistoryCallback callback = new HistoryCallback(sessions, sender);

        callback.handle(callbackUpdate("active_orders"), session);

        assertThat(session.getState()).isEqualTo(BotState.ACTIVE_ORDERS);
        verify(sender).sendActiveOrdersMenu(userId, 0);
    }

    @Test
    void orderCallbackOpensOrderPage() {
        OrderModel order = order(OrderStatus.PAID);
        when(orderService.findOrderForUser(order.getPaymentId(), userId)).thenReturn(Optional.of(order));

        OrderCallback callback = new OrderCallback(orderService, sessions, sender, localizationService);

        callback.handle(callbackUpdate("order:" + order.getPaymentId()), session);

        assertThat(session.getState()).isEqualTo(BotState.ORDER);
        verify(sender).sendOrderMessage(userId, order);
    }

    @Test
    void paidOrderMovesToHistory() {
        OrderModel order = order(OrderStatus.PENDING);
        when(orderService.findOrderForUser(order.getPaymentId(), userId)).thenReturn(Optional.of(order));
        when(orderService.confirmPayment(order.getPaymentId(), userId))
                .thenReturn(OrderService.PaymentConfirmationResult.PAID);

        OrderPaidCallback callback = new OrderPaidCallback(orderService, sessions, sender, localizationService);

        callback.handle(callbackUpdate("order_paid:" + order.getPaymentId()), session);

        assertThat(session.getState()).isEqualTo(BotState.HISTORY);
        verify(context).sendMessage(1L, "text");
        verify(sender).sendHistoryMenu(userId, 0);
    }

    @Test
    void unpaidOrderStaysOnOrderPage() {
        OrderModel order = order(OrderStatus.PENDING);
        when(orderService.findOrderForUser(order.getPaymentId(), userId)).thenReturn(Optional.of(order));
        when(orderService.confirmPayment(order.getPaymentId(), userId))
                .thenReturn(OrderService.PaymentConfirmationResult.NOT_PAID);

        OrderPaidCallback callback = new OrderPaidCallback(orderService, sessions, sender, localizationService);

        callback.handle(callbackUpdate("order_paid:" + order.getPaymentId()), session);

        verify(context).sendMessage(1L, "text");
        verify(sender).sendOrderMessage(userId, order);
    }

    @Test
    void cancelMovesOrderToHistory() {
        OrderModel order = order(OrderStatus.PENDING);
        when(orderService.findOrderForUser(order.getPaymentId(), userId)).thenReturn(Optional.of(order));
        when(orderService.cancelOrder(order.getPaymentId(), userId))
                .thenReturn(OrderService.OrderCancellationResult.CANCELED);

        OrderCancelCallback callback = new OrderCancelCallback(orderService, sessions, sender, localizationService);

        callback.handle(callbackUpdate("order_cancel:" + order.getPaymentId()), session);

        assertThat(session.getState()).isEqualTo(BotState.HISTORY);
        verify(context).sendMessage(1L, "text");
        verify(sender).sendHistoryMenu(userId, 0);
    }

    @Test
    void paginationOfHistoryAsksForHistoryMenu() {
        PageableCallback callback = new PageableCallback(sessions, sender);
        session.setState(BotState.HISTORY);
        session.setPage(0);

        callback.handle(callbackUpdate("page_forward"), session);

        assertThat(session.getPage()).isEqualTo(1);
        verify(sender).sendHistoryMenu(userId, 1);
    }

    private OrderModel order(OrderStatus status) {
        return OrderModel.builder()
                .paymentId(UUID.randomUUID())
                .status(status)
                .paymentLink("https://yookassa.test/pay")
                .build();
    }

    private BotUpdate callbackUpdate(String data) {
        return new BotUpdate(
                PlatformType.TELEGRAM, 1L, 1L, 10, "user",
                UpdateType.CALLBACK, null, "callback-id", data, context
        );
    }
}
