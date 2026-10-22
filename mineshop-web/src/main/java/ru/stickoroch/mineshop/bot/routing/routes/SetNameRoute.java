package ru.stickoroch.mineshop.bot.routing.routes;

import lombok.AllArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;
import ru.stickoroch.mineshop.web.service.UserService;

import java.util.regex.Pattern;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@AllArgsConstructor
public class SetNameRoute implements BotHandler {

    private static final Pattern MINECRAFT_NICKNAME = Pattern.compile("^[A-Za-z0-9_]{3,16}$");

    private final UserService userService;
    private final BotSessionService sessionService;
    private final LocalizationService localizationService;
    private final BotFormSender sender;

    @Override
    public boolean supports(
            BotUpdate update,
            BotSessionModel session
    ) {
        return session.getState() == BotState.RENAME && update.isText();
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

        name = name.trim();

        if (!MINECRAFT_NICKNAME.matcher(name).matches()) {
            update.context().sendMessage(
                    update.chatId(),
                    localizationService.getText(LocalizationKey.INVALID_NAME)
            );
            update.context().sendMessage(
                    update.chatId(),
                    localizationService.getText(LocalizationKey.WHAT_UR_NAME)
            );
            return;
        }

        userService.setMcName(
                update.platformUserId(),
                name
        );


        update.context().replyMessage(
                update.chatId(),
                update.messageId(),
                localizationService.getText(
                        LocalizationKey.NICE_TO_MEET_U,
                        localizationService.escapeMarkdown(name)
                )
        );

        sessionService.setState(
                update.platformUserId(),
                BotState.MENU
        );

        sender.sendMainMenu(session.getUserId());
    }
}
