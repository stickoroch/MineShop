package ru.stickoroch.mineshop.bot.telegram;

import io.github.natanimn.telebof.BotClient;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Message;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.stickoroch.mineshop.bot.pipeline.BotPipeline;
import ru.stickoroch.mineshop.bot.update.BotUpdate;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class TelegramBotExecutor {
    private static final int MAX_START_ATTEMPTS = 5;
    private static final long START_RETRY_DELAY_MS = 2_000L;

    BotClient telegramBotClient;
    Logger logger;
    BotPipeline pipeline;
    TelegramBotContext telegramBotContext;

    @PostConstruct
    public void run() {
        telegramBotClient.addHandler(new TelegramBotHandler(this));

        for (int attempt = 1; attempt <= MAX_START_ATTEMPTS; attempt++) {
            try {
                telegramBotClient.startPolling();
                logger.info("Telegram bot started!");
                return;
            } catch (Exception e) {
                logger.warn(
                        "Bad telegram start, attempt {}/{}: {}",
                        attempt,
                        MAX_START_ATTEMPTS,
                        e.getMessage()
                );
                sleep();
            }
        }

        logger.error("Telegram bot could not start after {} attempts", MAX_START_ATTEMPTS);
    }

    private void sleep() {
        try {
            Thread.sleep(START_RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void stop() {
        telegramBotClient.stop();
        logger.info("Telegram bot stopped");
    }

    public void handleCallBack(CallbackQuery callbackQuery) {
        logger.info("Handle telegram callback from {}:@{} - {}", callbackQuery.getFrom().getId(), callbackQuery.getFrom().getUsername(), callbackQuery.getData());
        pipeline.process(
                BotUpdate.callbackTelegram(telegramBotContext, callbackQuery)
        );
    }

    public void handleMessage(
            Message message
    ) {
        logger.info("Handle telegram message from {}:@{} - {}", message.getChat().getId(), message.getChat().getUsername(), message.getText());
        pipeline.process(
                BotUpdate.textTelegram(telegramBotContext, message)
        );
    }
}
