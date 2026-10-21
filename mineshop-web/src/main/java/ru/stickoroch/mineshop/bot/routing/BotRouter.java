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

        for (BotHandler handler : handlers) {
            try{
                if (handler.supports(update, session)) {
                    try {
                        handler.handle(update, session);

                        update.context().answerCallBackQuery(update.callbackId(), localizationService.getText(LocalizationKey.CALLBACK));
                        update.context().deleteMessage(update.chatId(), update.messageId());

                        return;
                    }catch (Exception e){
                        logger.error("Error in bot router handle! {}", handler.getClass().getName());
                        throw e;
                    }

                }
            }catch (Exception e){
                logger.error("Error in bot router(support or handler)! {}", handler.getClass().getName());
                e.printStackTrace();
                update.context().sendMessage(
                        update.chatId(),
                        "Ошибка( Обратитесь к администратору (/start)"
                );
                return;
            }

        }

        update.context().sendMessage(
                update.chatId(),
                "Я не понял команду. Используйте /start."
        );
    }
}