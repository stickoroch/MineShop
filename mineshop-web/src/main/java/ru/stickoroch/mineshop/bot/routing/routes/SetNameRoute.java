package ru.stickoroch.mineshop.bot.routing.routes;

import lombok.AllArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.form.BotFormSender;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;
import ru.stickoroch.mineshop.web.service.UserService;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@AllArgsConstructor
public class SetNameRoute implements BotHandler {

    private final UserService userService;
    private final BotSessionService sessionService;
    private final LocalizationService localizationService;
    private final BotFormSender sender;

    @Override
    public boolean supports(
            BotUpdate update,
            BotSessionModel session
    ) {
        return session.getState() == BotState.RENAME;
    }

    @Override
    public void handle(
            BotUpdate update,
            BotSessionModel session
    ) {

        if (!update.isText()) {
            return;
        }

        String name = update.text();

        if (name == null || name.isBlank()) {
            update.context().sendMessage(update.chatId(), localizationService.getText(LocalizationKey.INCORRECT_NAME));
            update.context().sendMessage(
                    update.chatId(),
                    localizationService.getText(LocalizationKey.WHAT_UR_NAME)
            );
            return;
        }

        //todo check name in mc server

        userService.setMcName(
                update.platformUserId(),
                name
        );


        update.context().replyMessage(update.chatId(), update.messageId(), localizationService.getText(LocalizationKey.NICE_TO_MEET_U, name));

        sessionService.setState(
                update.platformUserId(),
                BotState.MENU
        );

        sender.sendMainMenu(session.getUserId());
    }
}
