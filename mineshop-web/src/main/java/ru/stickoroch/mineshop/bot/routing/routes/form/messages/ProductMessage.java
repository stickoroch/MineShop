package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.bot.routing.routes.form.AbstractFormMessage;
import ru.stickoroch.mineshop.core.model.ProductModel;

public class ProductMessage extends AbstractFormMessage {
    public ProductMessage(LocalizationService localizationService, ProductModel productModel) {
        super(localizationService);

        this.text = localizationService.getText(LocalizationKey.PRODUCT_CARD, productModel.getTitle(), productModel.getDescription(), productModel.getPrice());

        this.inlineKeyboard = new BotInlineKeyboardMarkup();

        this.inlineKeyboard.addLine(
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.MENU_BUTTON), "mainMenu")
        );
    }
}
