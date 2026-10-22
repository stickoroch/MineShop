package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import org.springframework.data.domain.Page;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.bot.routing.routes.form.AbstractFormMessage;
import ru.stickoroch.mineshop.core.model.OrderModel;

import java.util.ArrayList;
import java.util.List;

public class OrdersMenu extends AbstractFormMessage {

    private static final int MAX_BUTTON_TEXT_LENGTH = 48;

    public OrdersMenu(LocalizationService localizationService,
                      String title,
                      String emptyText,
                      int page,
                      Page<OrderModel> orders) {
        super(localizationService);

        this.inlineKeyboard = new BotInlineKeyboardMarkup();

        List<OrderModel> content = orders.getContent();

        if (content.isEmpty()) {
            this.text = emptyText;
        } else {
            this.text = title;

            for (OrderModel order : content) {
                String label = order.getProduct().getTitle()
                        + " · " + OrderStatusText.text(localizationService, order.getStatus());

                this.inlineKeyboard.addLine(new BotInlineKeyboardButton(
                        shorten(label),
                        "order:" + order.getPaymentId()
                ));
            }
        }

        List<BotInlineKeyboardButton> pageButtons = new ArrayList<>();
        if (page > 0) {
            pageButtons.add(new BotInlineKeyboardButton(
                    localizationService.getText(LocalizationKey.PAGE_BACK),
                    "page_back"
            ));
        }
        if (orders.hasNext()) {
            pageButtons.add(new BotInlineKeyboardButton(
                    localizationService.getText(LocalizationKey.PAGE_FORWARD),
                    "page_forward"
            ));
        }
        if (!pageButtons.isEmpty()) {
            this.inlineKeyboard.addLine(pageButtons);
        }

        this.inlineKeyboard.addLine(
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.SHOP_BUTTON), "shop"),
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.MENU_BUTTON), "mainMenu")
        );
    }

    private String shorten(String text) {
        if (text.length() <= MAX_BUTTON_TEXT_LENGTH) {
            return text;
        }

        return text.substring(0, MAX_BUTTON_TEXT_LENGTH - 1) + "…";
    }
}
