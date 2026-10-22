package ru.stickoroch.mineshop.bot.routing.routes.callback;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.web.service.OrderService;
import ru.stickoroch.mineshop.web.service.OrderService.OrderCancellationResult;

import java.util.Optional;
import java.util.UUID;

/**
 * Обрабатывает кнопку «Отменить»: отменяет неоплаченный заказ
 * и переносит его в историю покупок.
 */
@Component
@AllArgsConstructor
public class OrderCancelCallback implements BotHandler {

    private static final String CALLBACK_PREFIX = "order_cancel:";

    private final OrderService orderService;
    private final BotSessionService sessions;
    private final BotFormSender sender;
    private final LocalizationService localizationService;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback()
                && update.callbackData() != null
                && update.callbackData().startsWith(CALLBACK_PREFIX);
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        UUID orderId = parseOrderId(update.callbackData());

        if (orderId == null || orderService.findOrderForUser(orderId, session.getUserId()).isEmpty()) {
            send(update, LocalizationKey.ORDER_NOT_FOUND);
            return;
        }

        OrderCancellationResult result = orderService.cancelOrder(orderId, session.getUserId());

        switch (result) {
            case CANCELED -> {
                send(update, LocalizationKey.ORDER_CANCELED_SUCCESS);

                sessions.update(session.getUserId(), sess -> {
                    sess.setState(BotState.HISTORY);
                    sess.setPage(0);
                });

                sender.sendHistoryMenu(session.getUserId(), 0);
            }
            case PAYMENT_ALREADY_PAID -> {
                send(update, LocalizationKey.ORDER_PAYMENT_ALREADY_PAID);
                showOrder(session, orderId);
            }
            case ALREADY_PROCESSED -> {
                send(update, LocalizationKey.ORDER_ALREADY_PROCESSED);
                showOrder(session, orderId);
            }
            case ERROR -> {
                send(update, LocalizationKey.ORDER_VERIFY_ERROR);
                showOrder(session, orderId);
            }
            case NOT_FOUND -> send(update, LocalizationKey.ORDER_NOT_FOUND);
        }
    }

    private void showOrder(BotSessionModel session, UUID orderId) {
        Optional<OrderModel> order = orderService.findOrderForUser(orderId, session.getUserId());
        order.ifPresent(model -> sender.sendOrderMessage(session.getUserId(), model));
    }

    private void send(BotUpdate update, LocalizationKey key) {
        update.context().sendMessage(
                update.chatId(),
                localizationService.getText(key)
        );
    }

    private UUID parseOrderId(String callbackData) {
        try {
            return UUID.fromString(callbackData.substring(CALLBACK_PREFIX.length()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
