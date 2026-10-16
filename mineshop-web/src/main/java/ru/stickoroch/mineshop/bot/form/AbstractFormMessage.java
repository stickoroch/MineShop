package ru.stickoroch.mineshop.bot.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.core.PlatformUserId;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractFormMessage implements BotFormMessage {

    protected final LocalizationService localizationService;
    protected String text;
    protected BotInlineKeyboardMarkup inlineKeyboard;

    public AbstractFormMessage(LocalizationService localizationService) {
        this.localizationService = localizationService;
    }

    public int sendMenu(long chatId, BotContext context) {
        return context.sendMenu(chatId, this);
    }
}
