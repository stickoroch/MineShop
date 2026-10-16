package ru.stickoroch.mineshop.bot.routing.routes.callback;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;


@Component
@AllArgsConstructor
public class ShopCallback implements BotHandler {

    private final BotSessionService sessions;
    private final LocalizationService localizationService;


    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback() && update.callbackData().equals("shop");
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        update.context().answerCallBackQuery(update.callbackId(), localizationService.getText(LocalizationKey.RENAME));
        update.context().deleteMessage(update.chatId(), update.messageId());

        sessions.setState(session.getUserId(), BotState.SHOP);


    }
}