package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import org.junit.jupiter.api.Test;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.model.ProductModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderMessageTest {

    private final LocalizationService localizationService = mock(LocalizationService.class);

    @Test
    void pendingOrderHasPaymentLinkAndActions() {
        OrderModel order = order(OrderStatus.PENDING);
        String paymentLink = order.getPaymentLink();

        stubLocalization();

        OrderMessage message = new OrderMessage(localizationService, order, null);

        List<BotInlineKeyboardButton> buttons = message.getInlineKeyboard().getButtons().stream()
                .flatMap(List::stream)
                .toList();

        BotInlineKeyboardButton payButton = buttons.stream()
                .filter(BotInlineKeyboardButton::isUrl)
                .findFirst()
                .orElseThrow();

        assertThat(payButton.getCallbackData()).isEqualTo(paymentLink);
        assertThat(buttons)
                .extracting(BotInlineKeyboardButton::getCallbackData)
                .contains(
                        "order_paid:" + order.getPaymentId(),
                        "order_cancel:" + order.getPaymentId(),
                        "active_orders",
                        "mainMenu"
                );
    }

    @Test
    void paidOrderHasNoPaymentActions() {
        OrderModel order = order(OrderStatus.PAID);

        stubLocalization();

        OrderMessage message = new OrderMessage(localizationService, order, null);

        List<BotInlineKeyboardButton> buttons = message.getInlineKeyboard().getButtons().stream()
                .flatMap(List::stream)
                .toList();

        assertThat(buttons)
                .extracting(BotInlineKeyboardButton::getCallbackData)
                .doesNotContain(
                        "order_paid:" + order.getPaymentId(),
                        "order_cancel:" + order.getPaymentId()
                )
                .contains("history", "mainMenu");
        assertThat(buttons).noneMatch(BotInlineKeyboardButton::isUrl);
    }

    private void stubLocalization() {
        when(localizationService.escapeMarkdown(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(localizationService.getText(any(LocalizationKey.class))).thenReturn("text");
        when(localizationService.getText(eq(LocalizationKey.ORDER_CARD), any(Object[].class)))
                .thenReturn("card");
    }

    private OrderModel order(OrderStatus status) {
        ProductModel product = ProductModel.builder()
                .id(5L)
                .title("Алмазный меч")
                .description("Острый клинок")
                .price(new BigDecimal("199.00"))
                .isActive(true)
                .build();

        return OrderModel.builder()
                .paymentId(UUID.randomUUID())
                .product(product)
                .amount(1)
                .cost(new BigDecimal("199.00"))
                .status(status)
                .paymentLink("https://yookassa.test/pay/" + UUID.randomUUID())
                .build();
    }
}
