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
public class PageableCallback implements BotHandler {
    BotSessionService botSessionService;
    BotFormSender botFormSender;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback() && (update.callbackData().equalsIgnoreCase("page_back") || update.callbackData().equalsIgnoreCase("page_forward"));
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        int offset = 1;
        if (update.callbackData().equalsIgnoreCase("page_back") && session.getPage() > 0) {
            offset = -1;
        }

        int page = session.getPage() + offset;

        botSessionService.update(session.getUserId(), sess -> {
            sess.setPage(page);
        });

        BotState state = session.getState();

        if (state == BotState.SHOP_POPULAR) {
            botFormSender.sendPopularShopMenu(session.getUserId(), page);
        }else if(state == BotState.SHOP_PRODUCTS){
            botFormSender.sendProductsShopMenu(session.getUserId(), page);
        }
    }
}
