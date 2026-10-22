package ru.stickoroch.mineshop.bot.routing.routes.callback;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;
import ru.stickoroch.mineshop.web.service.ProductService;

@Component
@AllArgsConstructor
public class ShopSearchCallback implements BotHandler {

    private static final String SEARCH_CALLBACK = "shop_search";

    private final BotSessionService sessions;
    private final BotFormSender sender;
    private final LocalizationService localizationService;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return (session.getState() == BotState.SHOP_SEARCH && update.isText())
                || (update.isCallback() && SEARCH_CALLBACK.equals(update.callbackData()));
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        if (update.isCallback()) {
            openSearchPrompt(session);
            return;
        }

        String query = ProductService.normalizeSearchQuery(update.text());

        if (query.length() < ProductService.MIN_SEARCH_QUERY_LENGTH) {
            update.context().sendMessage(
                    update.chatId(),
                    localizationService.getText(LocalizationKey.SHOP_SEARCH_SHORT)
            );
            sender.sendShopSearchMessage(session.getUserId());
            return;
        }

        sessions.update(session.getUserId(), sess -> {
            sess.setState(BotState.SHOP_SEARCH);
            sess.setSearchQuery(query);
            sess.setPage(0);
        });

        sender.sendShopSearchResults(session.getUserId(), query, 0);
    }

    private void openSearchPrompt(BotSessionModel session) {
        sessions.update(session.getUserId(), sess -> {
            sess.setState(BotState.SHOP_SEARCH);
            sess.setSearchQuery(null);
            sess.setPage(0);
        });

        sender.sendShopSearchMessage(session.getUserId());
    }
}
