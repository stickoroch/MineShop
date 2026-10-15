package ru.stickoroch.mineshop.bot.telegram;

import io.github.natanimn.telebof.BotClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;

@Configuration
public class TelegramBotConfiguration {
    private final BotClient telegramBotClient;

    public TelegramBotConfiguration(@Value("${app.bot.telegram.token}") String token,
                                    @Value("${app.bot.telegram.proxy.address}") String telegramProxyParam,
                                    @Value("${app.bot.telegram.proxy.enabled}") boolean proxyEnabled)  {
        BotClient.Builder builder = new BotClient
                .Builder(token);

        if (proxyEnabled) {
            String[] t = telegramProxyParam.split(":");
            SocketAddress telegramProxy = new InetSocketAddress(t[0], Integer.parseInt(t[1]));
            builder = builder.proxy(new Proxy(Proxy.Type.SOCKS, telegramProxy));
        }

        this.telegramBotClient =
                builder
                        .build();
    }


    @Bean
    public BotClient getTelegramBotClient() {
        return telegramBotClient;
    }
}
