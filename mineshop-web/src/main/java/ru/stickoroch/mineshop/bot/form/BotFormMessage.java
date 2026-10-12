package ru.stickoroch.mineshop.bot.form;

import lombok.NonNull;
import ru.stickoroch.mineshop.bot.context.BotContext;
import ru.stickoroch.mineshop.bot.context.callback.BotInlineKeyboardMarkup;

public interface BotFormMessage {

    @NonNull
    String getText();

    BotInlineKeyboardMarkup getInlineKeyboard();


    int sendMenu(BotContext context, long chatId);
}
