package ru.stickoroch.mineshop.bot.routing.routes.callback;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;

@Component
@AllArgsConstructor
public class ShopSearchCallback implements BotHandler {

    private final BotSessionService sessions;
    private final BotFormSender sender;
    private final LocalizationService localizationService;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return (session.getState() == BotState.SHOP_SEARCH && update.isText())
                || (update.isCallback() && update.callbackData().equalsIgnoreCase("shop_search"));
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        if(update.isCallback() && update.callbackData().equalsIgnoreCase("shop_search")){
            sessions.setState(session.getUserId(), BotState.SHOP_SEARCH);

            sender.sendShopSearchMessage(session.getUserId());
            return;
        }

        if(session.getState() == BotState.SHOP_SEARCH && update.isText())
            //todo search
    }
}
