package ru.stickoroch.mineshop.bot.routing;

import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.update.BotUpdate;

public interface BotHandler {

    boolean supports(
            BotUpdate update,
            BotSessionModel session
    );

    void handle(
            BotUpdate update,
            BotSessionModel session
    );
}