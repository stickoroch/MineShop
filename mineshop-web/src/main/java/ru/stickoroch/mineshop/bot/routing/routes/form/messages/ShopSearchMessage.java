package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.bot.routing.routes.form.AbstractFormMessage;

public class ShopSearchMessage extends AbstractFormMessage {

    public ShopSearchMessage(LocalizationService localizationService) {
        super(localizationService);

        this.text = localizationService.getText(LocalizationKey.SHOP_TITLE);

        this.inlineKeyboard = new BotInlineKeyboardMarkup();
        this.inlineKeyboard.addLine(new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.SHOP_BUTTON), "shop"));
        this.inlineKeyboard.addLine(new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.MAIN_MENU), "mainMenu"));

    }
}