package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.core.OrderStatus;

public final class OrderStatusText {

    private OrderStatusText() {
    }

    public static String text(LocalizationService localizationService, OrderStatus status) {
        LocalizationKey key = switch (status) {
            case PENDING -> LocalizationKey.ORDER_STATUS_PENDING;
            case PAID -> LocalizationKey.ORDER_STATUS_PAID;
            case DELIVERED -> LocalizationKey.ORDER_STATUS_DELIVERED;
            case PENDING_DELIVERY -> LocalizationKey.ORDER_STATUS_PENDING_DELIVERY;
            case CANCELED -> LocalizationKey.ORDER_STATUS_CANCELED;
        };

        return localizationService.getText(key);
    }
}
