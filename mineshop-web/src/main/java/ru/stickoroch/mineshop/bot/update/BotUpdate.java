package ru.stickoroch.mineshop.bot.update;

import io.github.natanimn.telebof.types.chat_and_user.Chat;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Message;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.core.PlatformType;
import ru.stickoroch.mineshop.core.PlatformUserId;

public record BotUpdate(
        PlatformType platform,
        long userId,
        long chatId,
        Integer messageId,
        String username,
        UpdateType type,
        String text,
        String callbackId,
        String callbackData,
        BotContext context
) {

    public static BotUpdate textTelegram(
            BotContext context,
            Message message
    ) {
        return new BotUpdate(
                PlatformType.TELEGRAM,
                message.getChat().getId(),
                message.getChat().getId(),
                message.getMessageId(),
                message.getChat().getUsername(),
                UpdateType.TEXT,
                message.getText(),
                null,
                null,
                context
        );
    }

    public static BotUpdate callbackTelegram(BotContext context,
                                     CallbackQuery callbackQuery) {
        Chat chat = callbackQuery.getMessage().getChat();
        return new BotUpdate(
                PlatformType.TELEGRAM,
                chat.getId(),
                chat.getId(),
                callbackQuery.getMessage().getMessageId(),
                chat.getUsername(),
                UpdateType.CALLBACK,
                null,
                callbackQuery.getId(),
                callbackQuery.getData(),
                context
        );
    }

    public boolean isText() {
        return type == UpdateType.TEXT;
    }

    public boolean isCallback() {
        return type == UpdateType.CALLBACK;
    }

    public PlatformUserId platformUserId() {
        return new PlatformUserId(userId, platform);
    }
}