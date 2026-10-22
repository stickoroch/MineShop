package ru.stickoroch.mineshop.bot.routing.routes.callback;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;

/**
 * Открывает историю покупок или список активных (неоплаченных) заказов.
 */
@Component
@AllArgsConstructor
public class HistoryCallback implements BotHandler {

    private static final String HISTORY = "history";
    private static final String ACTIVE_ORDERS = "active_orders";

    private final BotSessionService sessions;
    private final BotFormSender sender;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback()
                && (HISTORY.equals(update.callbackData()) || ACTIVE_ORDERS.equals(update.callbackData()));
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        boolean activeOrders = ACTIVE_ORDERS.equals(update.callbackData());

        sessions.update(session.getUserId(), sess -> {
            sess.setState(activeOrders ? BotState.ACTIVE_ORDERS : BotState.HISTORY);
            sess.setPage(0);
        });

        if (activeOrders) {
            sender.sendActiveOrdersMenu(session.getUserId(), 0);
        } else {
            sender.sendHistoryMenu(session.getUserId(), 0);
        }
    }
}
