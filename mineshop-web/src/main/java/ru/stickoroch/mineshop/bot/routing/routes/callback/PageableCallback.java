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
    private static final String PAGE_BACK = "page_back";
    private static final String PAGE_FORWARD = "page_forward";

    private final BotSessionService botSessionService;
    private final BotFormSender botFormSender;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback()
                && (PAGE_BACK.equals(update.callbackData()) || PAGE_FORWARD.equals(update.callbackData()));
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        BotState state = session.getState();

        int offset = PAGE_FORWARD.equals(update.callbackData()) ? 1 : -1;
        int page = Math.max(0, session.getPage() + offset);

        botSessionService.update(session.getUserId(), sess -> sess.setPage(page));

        switch (state) {
            case SHOP_POPULAR -> botFormSender.sendPopularShopMenu(session.getUserId(), page);
            case SHOP_PRODUCTS -> botFormSender.sendProductsShopMenu(session.getUserId(), page);
            case SHOP_SEARCH -> {
                String query = session.getSearchQuery();

                if (query == null || query.isBlank()) {
                    botFormSender.sendShopSearchMessage(session.getUserId());
                } else {
                    botFormSender.sendShopSearchResults(session.getUserId(), query, page);
                }
            }
            case HISTORY -> botFormSender.sendHistoryMenu(session.getUserId(), page);
            case ACTIVE_ORDERS -> botFormSender.sendActiveOrdersMenu(session.getUserId(), page);
            default -> botFormSender.sendMainMenu(session.getUserId());
        }
    }
}
