package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.bot.routing.routes.form.AbstractFormMessage;
import ru.stickoroch.mineshop.core.model.ProductModel;

import java.io.File;

public class ProductMessage extends AbstractFormMessage {

    private static final int MAX_DESCRIPTION_LENGTH = 700;

    private final File coverFile;

    public ProductMessage(LocalizationService localizationService, ProductModel productModel, File coverFile) {
        super(localizationService);

        this.coverFile = coverFile;

        this.text = localizationService.getText(
                LocalizationKey.PRODUCT_CARD,
                localizationService.escapeMarkdown(productModel.getTitle()),
                localizationService.escapeMarkdown(shortenDescription(productModel.getDescription())),
                formatPrice(productModel)
        );

        this.inlineKeyboard = new BotInlineKeyboardMarkup();

        this.inlineKeyboard.addLine(
                new BotInlineKeyboardButton(
                        localizationService.getText(LocalizationKey.BUY_BUTTON),
                        "buy:" + productModel.getId()
                )
        );

        this.inlineKeyboard.addLine(
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.SHOP_BUTTON), "shop"),
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.MENU_BUTTON), "mainMenu")
        );
    }

    @Override
    public int sendMenu(long chatId, BotContext context) {
        if (coverFile != null && coverFile.isFile()) {
            return context.sendPhoto(chatId, this.text, coverFile, this.inlineKeyboard);
        }

        return context.sendMessage(chatId, this.text, this.inlineKeyboard);
    }

    private String shortenDescription(String description) {
        if (description == null || description.isBlank()) {
            return "—";
        }

        String normalized = description.trim();

        if (normalized.length() <= MAX_DESCRIPTION_LENGTH) {
            return normalized;
        }

        return normalized.substring(0, MAX_DESCRIPTION_LENGTH - 1) + "…";
    }

    private String formatPrice(ProductModel productModel) {
        if (productModel.getPrice() == null) {
            return "—";
        }

        return productModel.getPrice().stripTrailingZeros().toPlainString();
    }
}
