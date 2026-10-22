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

import java.util.Optional;
import java.util.UUID;

/**
 * Открывает страницу уже созданного заказа (например, из истории покупок).
 */
@Component
@AllArgsConstructor
public class OrderCallback implements BotHandler {

    private static final String ORDER_CALLBACK_PREFIX = "order:";

    private final OrderService orderService;
    private final BotSessionService sessions;
    private final BotFormSender sender;
    private final LocalizationService localizationService;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback()
                && update.callbackData() != null
                && update.callbackData().startsWith(ORDER_CALLBACK_PREFIX);
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        UUID orderId = parseOrderId(update.callbackData());
        Optional<OrderModel> order = orderId == null
                ? Optional.empty()
                : orderService.findOrderForUser(orderId, session.getUserId());

        if (order.isEmpty()) {
            update.context().sendMessage(
                    update.chatId(),
                    localizationService.getText(LocalizationKey.ORDER_NOT_FOUND)
            );
            return;
        }

        sessions.update(session.getUserId(), sess -> sess.setState(BotState.ORDER));
        sender.sendOrderMessage(session.getUserId(), order.get());
    }

    static UUID parseOrderId(String callbackData) {
        try {
            return UUID.fromString(callbackData.substring(ORDER_CALLBACK_PREFIX.length()));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
