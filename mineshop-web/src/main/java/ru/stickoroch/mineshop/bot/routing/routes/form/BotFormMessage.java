package ru.stickoroch.mineshop.bot.routing.routes.form;

import lombok.NonNull;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;

public interface BotFormMessage {

    @NonNull
    String getText();

    BotInlineKeyboardMarkup getInlineKeyboard();

    int sendMenu(long chatId, BotContext context);
}
