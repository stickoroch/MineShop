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
    BotClient telegramBotClient;
    Logger logger;
    BotPipeline pipeline;
    TelegramBotContext telegramBotContext;

    @PostConstruct
    public void run() {
        telegramBotClient.addHandler(new TelegramBotHandler(this));
        for (int i = 0; i < 20; i++) {
            try {
                telegramBotClient.startPolling();
                break;
            }catch (Exception e){
                logger.info("Bad telegram start, try another one {}/20", i);
            }
        }

        logger.info("Telegram bot started!");
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
