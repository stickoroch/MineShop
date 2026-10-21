package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.routing.routes.form.AbstractFormMessage;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.core.model.UserModel;

public class MainMenu extends AbstractFormMessage {


    public MainMenu(LocalizationService localizationService, UserModel userModel) {
        super(localizationService);

        this.text = localizationService.getText(LocalizationKey.MAIN_MENU, userModel.getMcNickname());

        this.inlineKeyboard = new BotInlineKeyboardMarkup();
        this.inlineKeyboard.addLine(new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.SHOP_BUTTON), "shop"));
        this.inlineKeyboard.addLine(
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.HISTORY_BUTTON), "history"),
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.ACTIVE_ORDERS_BUTTON), "active_orders")
        );
        this.inlineKeyboard.addLine(new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.RENAME), "rename"));


    }
}
