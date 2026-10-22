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
public class SetNameCallback implements BotHandler {

    private final BotSessionService sessions;
    private final LocalizationService localizationService;


    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback() && "rename".equals(update.callbackData());
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        sessions.setState(
                update.platformUserId(),
                BotState.RENAME
        );

        update.context().sendMessage(
                update.chatId(),
                localizationService.getText(LocalizationKey.WHAT_UR_NAME)
        );
    }
}
