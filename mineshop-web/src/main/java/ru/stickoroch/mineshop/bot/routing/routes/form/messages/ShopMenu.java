package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.routing.routes.form.AbstractFormMessage;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;

public class ShopMenu extends AbstractFormMessage {

    public ShopMenu(LocalizationService localizationService) {
        super(localizationService);

        this.inlineKeyboard = new BotInlineKeyboardMarkup();

        this.inlineKeyboard.addLine(
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.POPULAR_PRODUCTS_BUTTON), "popular_shop")
        );
        this.inlineKeyboard.addLine(
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.SHOP_SEARCH_BUTTON), "shop_search")
        );
        this.inlineKeyboard.addLine(
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.SHOP_PRODUCTS_BUTTON), "products")
        );
        this.inlineKeyboard.addLine(
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.MENU_BUTTON), "mainMenu")
        );
    }
}
