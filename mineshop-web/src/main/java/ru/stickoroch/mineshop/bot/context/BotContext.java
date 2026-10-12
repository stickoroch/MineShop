package ru.stickoroch.mineshop.bot.context;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import ru.stickoroch.mineshop.bot.context.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.bot.form.BotFormMessage;

import java.io.File;

public interface BotContext {

    int sendMessage(long chatId, @NonNull String text);

    int replyMessage(long chatId, int replyMessageId, @NonNull String text);

    int sendMessage(long chatId, @NonNull String text, BotInlineKeyboardMarkup markup);

    default int sendMenu(long chatId, @NonNull BotFormMessage menuMessage) {
        return sendMessage(chatId, menuMessage.getText(), menuMessage.getInlineKeyboard());
    }

    boolean deleteMessage(long chatId, int messageId);

    void answerCallBackQuery(@NonNull String callbackId, @NonNull String text);

    int sendPhoto(long chatId, @Nullable String text, @NonNull File photo, @Nullable BotInlineKeyboardMarkup markup);

}
