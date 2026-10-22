package ru.stickoroch.mineshop.bot.routing;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.update.BotUpdate;

import java.util.List;

@Component
public class BotRouter {

    private final List<BotHandler> handlers;
    private final Logger logger;
    private final LocalizationService localizationService;

    public BotRouter(List<BotHandler> handlers, Logger logger, LocalizationService localizationService) {
        this.handlers = handlers;
        this.logger = logger;
        this.localizationService = localizationService;
    }

    public void route(
            BotUpdate update,
            BotSessionModel session
    ) {
        answerCallback(update);

        for (BotHandler handler : handlers) {
            boolean supported;

            try {
                supported = handler.supports(update, session);
            } catch (Exception e) {
                logger.error("Error in bot router(supports)! {}", handler.getClass().getName(), e);
                sendError(update);
                return;
            }

            if (!supported) {
                continue;
            }

            try {
                handler.handle(update, session);
            } catch (Exception e) {
                logger.error("Error in bot router handle! {}", handler.getClass().getName(), e);
                sendError(update);
                return;
            }

            deletePreviousMessage(update);

            return;
        }

        update.context().sendMessage(
                update.chatId(),
                localizationService.getText(LocalizationKey.UNKNOWN_COMMAND)
        );
    }

    private void answerCallback(BotUpdate update) {
        if (!update.isCallback() || update.callbackId() == null) {
            return;
        }

        try {
            update.context().answerCallBackQuery(
                    update.callbackId(),
                    localizationService.getText(LocalizationKey.CALLBACK)
            );
        } catch (Exception e) {
            logger.warn("Could not answer telegram callback {}", update.callbackId(), e);
        }
    }

    private void deletePreviousMessage(BotUpdate update) {
        if (update.messageId() == null) {
            return;
        }

        try {
            update.context().deleteMessage(update.chatId(), update.messageId());
        } catch (Exception e) {
            logger.warn("Could not delete previous bot message {}", update.messageId(), e);
        }
    }

    private void sendError(BotUpdate update) {
        try {
            update.context().sendMessage(
                    update.chatId(),
                    localizationService.getText(LocalizationKey.BOT_ERROR)
            );
        } catch (Exception e) {
            logger.error("Could not send error message to chat {}", update.chatId(), e);
        }
    }
}
