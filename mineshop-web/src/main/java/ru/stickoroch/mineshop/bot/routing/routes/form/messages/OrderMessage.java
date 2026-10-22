package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.bot.routing.routes.form.AbstractFormMessage;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.model.ProductModel;

import java.io.File;

public class OrderMessage extends AbstractFormMessage {

    private static final int MAX_DESCRIPTION_LENGTH = 600;
    private static final int SHORT_ID_LENGTH = 8;

    private final File coverFile;

    public OrderMessage(LocalizationService localizationService,
                        OrderModel orderModel,
                        File coverFile) {
        super(localizationService);

        this.coverFile = coverFile;

        ProductModel product = orderModel.getProduct();

        this.text = localizationService.getText(
                LocalizationKey.ORDER_CARD,
                localizationService.escapeMarkdown(product.getTitle()),
                localizationService.escapeMarkdown(shortenDescription(product.getDescription())),
                formatPrice(orderModel),
                OrderStatusText.text(localizationService, orderModel.getStatus()),
                shortId(orderModel)
        );

        this.inlineKeyboard = new BotInlineKeyboardMarkup();

        if (orderModel.getStatus() == OrderStatus.PENDING) {
            if (orderModel.getPaymentLink() != null && !orderModel.getPaymentLink().isBlank()) {
                this.inlineKeyboard.addLine(new BotInlineKeyboardButton(
                        localizationService.getText(LocalizationKey.ORDER_PAY_BUTTON),
                        orderModel.getPaymentLink(),
                        true
                ));
            }

            this.inlineKeyboard.addLine(new BotInlineKeyboardButton(
                    localizationService.getText(LocalizationKey.ORDER_PAID_BUTTON),
                    "order_paid:" + orderModel.getPaymentId()
            ));

            this.inlineKeyboard.addLine(new BotInlineKeyboardButton(
                    localizationService.getText(LocalizationKey.ORDER_CANCEL_BUTTON),
                    "order_cancel:" + orderModel.getPaymentId()
            ));
        }

        boolean pending = orderModel.getStatus() == OrderStatus.PENDING;

        this.inlineKeyboard.addLine(
                pending
                        ? new BotInlineKeyboardButton(
                                localizationService.getText(LocalizationKey.ACTIVE_ORDERS_BUTTON),
                                "active_orders")
                        : new BotInlineKeyboardButton(
                                localizationService.getText(LocalizationKey.HISTORY_BUTTON),
                                "history"),
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

    private String formatPrice(OrderModel orderModel) {
        if (orderModel.getCost() == null) {
            return "—";
        }

        return orderModel.getCost().stripTrailingZeros().toPlainString();
    }

    private String shortId(OrderModel orderModel) {
        String id = orderModel.getPaymentId().toString().replace("-", "");
        return id.substring(0, Math.min(SHORT_ID_LENGTH, id.length())).toUpperCase();
    }
}
