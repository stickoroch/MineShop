package ru.stickoroch.mineshop.bot.telegram;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.enums.ParseMode;
import io.github.natanimn.telebof.requests.send.SendMessage;
import io.github.natanimn.telebof.requests.send.SendPhoto;
import io.github.natanimn.telebof.types.ReplyParameters;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardButton;
import io.github.natanimn.telebof.types.keyboard.InlineKeyboardMarkup;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.core.PlatformType;

import java.io.File;
import java.util.List;


@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Component
public class TelegramBotContext implements BotContext {

    io.github.natanimn.telebof.BotContext telegramBotContext;

    public TelegramBotContext(BotClient telegramBotClient) {
        this.telegramBotContext = telegramBotClient.context;
    }


    @Override
    public int sendMessage(long chatId, @NonNull String text) {
        SendMessage mes = telegramBotContext.sendMessage(chatId, text);
        mes = mes.parseMode(ParseMode.MARKDOWN);
        return mes.exec().getMessageId();
    }

    @Override
    public int replyMessage(long chatId, int replyMessageId, @NonNull String text) {
        SendMessage mes = telegramBotContext.sendMessage(chatId, text);
        mes = mes.parseMode(ParseMode.MARKDOWN);
        return mes.replyParameters(ReplyParameters.ofRegular(replyMessageId)).exec().getMessageId();
    }

    @Override
    public int sendMessage(long chatId, @NonNull String text, BotInlineKeyboardMarkup inline) {
        SendMessage mes = telegramBotContext.sendMessage(chatId, text);
        if (inline != null)
            mes = mes.replyMarkup(toTelegramInlineKeyboard(inline));
        mes = mes.parseMode(ParseMode.MARKDOWN);
        return mes.exec().getMessageId();
    }


    @Override
    public boolean deleteMessage(long chatId, int messageId) {
        try {
            return telegramBotContext.deleteMessage(chatId, messageId).exec();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void answerCallBackQuery(@NonNull String callbackId, @NonNull String text) {
        io.github.natanimn.telebof.requests.answer.AnswerCallbackQuery request =
                telegramBotContext.answerCallbackQuery(callbackId);

        if (!text.isBlank()) {
            request = request.text(text);
        }

        request.exec();
    }

    @Override
    public int sendPhoto(long chatId, @Nullable String text, @NonNull File photo, @Nullable BotInlineKeyboardMarkup markup) {
        SendPhoto sendPhoto = telegramBotContext.sendPhoto(chatId, photo);
        if (text != null) {
            sendPhoto = sendPhoto.caption(text);
        }
        if (markup != null) {
            sendPhoto = sendPhoto.replyMarkup(toTelegramInlineKeyboard(markup));
        }
        sendPhoto = sendPhoto.parseMode(ParseMode.MARKDOWN);
        return sendPhoto.exec().getMessageId();
    }

    @Override
    public @NonNull PlatformType getPlatformType() {
        return PlatformType.TELEGRAM;
    }

    private InlineKeyboardMarkup toTelegramInlineKeyboard(BotInlineKeyboardMarkup markup) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        for (List<BotInlineKeyboardButton> line : markup.getButtons()) {
            InlineKeyboardButton[] buttons = line.stream()
                    .map(x -> {
                        if (x.isUrl()) {
                            return new InlineKeyboardButton(x.getText()).setUrl(x.getCallbackData());
                        } else {
                            return new InlineKeyboardButton(x.getText(), x.getCallbackData());
                        }
                    })
                    .toArray(InlineKeyboardButton[]::new);

            keyboardMarkup.addKeyboard(buttons);
        }

        return keyboardMarkup;
    }

}
