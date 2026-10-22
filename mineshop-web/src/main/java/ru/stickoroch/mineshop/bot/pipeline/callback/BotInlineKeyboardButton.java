package ru.stickoroch.mineshop.bot.pipeline.callback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BotInlineKeyboardButton {



    @NonNull
    private String text;
    @NonNull
    private String callbackData;
    boolean url;

    public BotInlineKeyboardButton(@NonNull String text, @NonNull String callbackData) {
        this.text = text;
        this.callbackData = callbackData;
        this.url = false;
    }
}
