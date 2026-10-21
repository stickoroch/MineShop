package ru.stickoroch.mineshop.bot.pipeline;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormMessage;
import ru.stickoroch.mineshop.core.PlatformType;

import java.io.File;

@Component
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

    @NonNull
    PlatformType getPlatformType();

}
