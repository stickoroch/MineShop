package ru.stickoroch.mineshop.web.plugin;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Настройки моста «бэкенд — Minecraft-плагин».
 */
@Getter
@Setter
@Component
@ConfigurationProperties("app.plugin")
public class PluginProperties {

    /**
     * Общий секрет. Плагин присылает его в первом сообщении hello.
     */
    private String token = "change-me";

    /**
     * После скольких неудачных попыток заказ уходит в ручную выдачу.
     */
    private int maxAttempts = 10;

    /**
     * Через сколько минут «зависшая» попытка выдачи считается брошенной.
     */
    private int staleInProgressMinutes = 2;

    private int sessionTimeoutSeconds = 90;
}
