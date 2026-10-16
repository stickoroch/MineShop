package ru.stickoroch.mineshop.bot.routing.routes;

import lombok.RequiredArgsConstructor;
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
import ru.stickoroch.mineshop.core.model.UserModel;
import ru.stickoroch.mineshop.web.service.UserService;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class StartRoute implements BotHandler {

    private final BotSessionService sessions;
    private final UserService userService;
    private final LocalizationService localizationService;
    private final BotFormSender botFormSender;

    @Override
    public boolean supports(
            BotUpdate update,
            BotSessionModel session
    ) {
        return update.isText()
                && "/start".equalsIgnoreCase(update.text());
    }

    @Override
    public void handle(
            BotUpdate update,
            BotSessionModel session
    ) {
        UserModel student =
                userService.getUser(update.platformUserId());

        if (student.getMcNickname() == null || student.getMcNickname().equalsIgnoreCase("-") || student.getMcNickname().isBlank()) {

            sessions.setState(
                    update.platformUserId(),
                    BotState.RENAME
            );

            update.context().sendMessage(
                    update.chatId(),
                    localizationService.getText(LocalizationKey.WHAT_UR_NAME)
            );

            return;
        }

        sessions.setState(
                update.platformUserId(),
                BotState.MENU
        );

        botFormSender.sendMainMenu(session.getUserId());
    }
}