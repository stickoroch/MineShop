package ru.stickoroch.mineshop.bot.routing.routes.callback;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
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

@Component
@AllArgsConstructor
public class BuyProductCallback implements BotHandler {

    private static final String BUY_CALLBACK_PREFIX = "buy:";

    private final OrderService orderService;
    private final BotSessionService sessions;
    private final BotFormSender sender;
    private final LocalizationService localizationService;
    private final Logger logger;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback()
                && update.callbackData() != null
                && update.callbackData().startsWith(BUY_CALLBACK_PREFIX);
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        Long productId = parseProductId(update.callbackData());

        if (productId == null) {
            send(update, LocalizationKey.PRODUCT_NOT_FOUND);
            return;
        }

        try {
            OrderModel order = orderService.createOrder(update.platformUserId(), productId);

            sessions.update(session.getUserId(), sess -> {
                sess.setState(BotState.ORDER);
                sess.setProductId(productId);
            });

            sender.sendOrderMessage(session.getUserId(), order);
        } catch (IllegalArgumentException e) {
            logger.warn("Could not create order for product {}: {}", productId, e.getMessage());
            send(update, LocalizationKey.PRODUCT_NOT_FOUND);
        } catch (Exception e) {
            logger.error("Could not create order for product {}", productId, e);
            send(update, LocalizationKey.ORDER_CREATE_ERROR);
        }
    }

    private Long parseProductId(String callbackData) {
        try {
            return Long.parseLong(callbackData.substring(BUY_CALLBACK_PREFIX.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void send(BotUpdate update, LocalizationKey key) {
        update.context().sendMessage(
                update.chatId(),
                localizationService.getText(key)
        );
    }
}
