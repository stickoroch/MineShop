package ru.stickoroch.mineshop.bot.form;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.stickoroch.mineshop.bot.form.messages.MainMenu;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.bot.pipeline.BotRegistry;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.web.service.UserService;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BotFormSender {

    BotRegistry botRegistry;
    LocalizationService localizationService;
    UserService userService;

    private BotContext getContextForUser(PlatformUserId user){
        return botRegistry.getBotContext(user.getPlatform());
    }

    public void sendMainMenu(PlatformUserId userId){
        BotContext context = getContextForUser(userId);

        new MainMenu(localizationService, userService.getUser(userId)).sendMenu(userId.getPlatformId(), context);
    }

}
