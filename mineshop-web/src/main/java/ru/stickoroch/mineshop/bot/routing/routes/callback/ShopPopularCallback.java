package ru.stickoroch.mineshop.bot.routing.routes.callback;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;


@Component
@AllArgsConstructor
public class ShopPopularCallback implements BotHandler {

    private final BotSessionService sessions;
    private final BotFormSender sender;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback() && update.callbackData().equalsIgnoreCase("popular_shop");
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        sessions.update(session.getUserId(), sess -> {
            sess.setState(BotState.SHOP_POPULAR);
            sess.setPage(0);
        });

        sender.sendPopularShopMenu(session.getUserId(), 0);
    }
}