package ru.stickoroch.mineshop.bot.pipeline.callback;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class BotInlineKeyboardMarkup {

    private List<List<BotInlineKeyboardButton>> buttons = new ArrayList<>();

    public void addLine(@NonNull BotInlineKeyboardButton... rowButtons) {
        buttons.add(Arrays.stream(rowButtons).toList());
    }

    public void addLine(@NonNull List<BotInlineKeyboardButton> rowButtons) {
        buttons.add(rowButtons);
    }

}
