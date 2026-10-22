package ru.stickoroch.mineshop.bot.routing.routes.callback;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;

@Component
@AllArgsConstructor
public class MainMenuCallback implements BotHandler {

    BotFormSender sender;
    BotSessionService sessions;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback() && "mainMenu".equals(update.callbackData());
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        sessions.update(session.getUserId(), sess->{
           sess.setState(BotState.MENU);
           sess.setPage(0);
        });

        sender.sendMainMenu(session.getUserId());
    }
}