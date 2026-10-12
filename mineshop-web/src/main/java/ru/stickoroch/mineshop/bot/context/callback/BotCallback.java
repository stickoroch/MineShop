package ru.stickoroch.mineshop.bot.context.callback;


public record BotCallback(
        String action,
        Long id
) {

    public static BotCallback parse(String data) {

        String[] parts = data.split(":");

        return new BotCallback(
                parts[0],
                parts.length > 1
                        ? Long.parseLong(parts[1])
                        : null
        );
    }
}