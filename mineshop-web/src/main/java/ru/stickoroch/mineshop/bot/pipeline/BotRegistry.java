package ru.stickoroch.mineshop.bot.pipeline;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.stickoroch.mineshop.core.PlatformType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BotRegistry {
    private final Map<PlatformType, BotContext> botByType = new HashMap<>();

    public BotRegistry(List<BotContext> bots) {
        for (BotContext bot : bots) {
            botByType.put(bot.getPlatformType(), bot);
        }
    }

    @NonNull
    public BotContext getBotContext(@NonNull PlatformType type) {
        return botByType.get(type);
    }

}
