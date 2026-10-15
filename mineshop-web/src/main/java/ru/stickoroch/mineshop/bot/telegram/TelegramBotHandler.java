package ru.stickoroch.mineshop.bot.telegram;

import io.github.natanimn.telebof.BotContext;
import io.github.natanimn.telebof.annotations.CallbackHandler;
import io.github.natanimn.telebof.annotations.MessageHandler;
import io.github.natanimn.telebof.enums.ChatType;
import io.github.natanimn.telebof.enums.MessageType;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Message;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public class TelegramBotHandler {

    @NonNull
    private final TelegramBotExecutor telegramBotExecutor;

    @MessageHandler(type = MessageType.TEXT, chatType = ChatType.PRIVATE)
    void onText(
            BotContext context,
            Message message
    ) {
        telegramBotExecutor.handleMessage(
                message
        );
    }

    @CallbackHandler(chatType = ChatType.PRIVATE)
    void onCallback(
            BotContext botContext,
            CallbackQuery callbackQuery
    ) {
        telegramBotExecutor.handleCallBack(
                callbackQuery
        );
    }
}
