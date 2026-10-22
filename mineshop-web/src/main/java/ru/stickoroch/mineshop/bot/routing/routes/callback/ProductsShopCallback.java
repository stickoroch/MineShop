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
public class ProductsShopCallback implements BotHandler {

    private final BotSessionService sessions;
    private final BotFormSender sender;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback() && "products".equals(update.callbackData());
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        sessions.update(session.getUserId(), sess->{
          sess.setState( BotState.SHOP_PRODUCTS);
          sess.setPage(0);
        });


        sender.sendProductsShopMenu(session.getUserId(), 0);
    }
}