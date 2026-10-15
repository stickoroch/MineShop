package ru.stickoroch.mineshop.bot.form;

import lombok.NonNull;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.core.PlatformUserId;

public interface BotFormMessage {

    @NonNull
    String getText();

    BotInlineKeyboardMarkup getInlineKeyboard();


    int sendMenu(PlatformUserId platformUserId);
}
