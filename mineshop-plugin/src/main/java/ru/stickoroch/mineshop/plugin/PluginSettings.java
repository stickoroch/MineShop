package ru.stickoroch.mineshop.plugin;

import org.bukkit.configuration.file.FileConfiguration;

public record PluginSettings(
        String backendUrl,
        String token,
        int reconnectSeconds
) {

    public static PluginSettings from(FileConfiguration config) {
        return new PluginSettings(
                config.getString("backend-url", "ws://127.0.0.1:8080/ws/plugin"),
                config.getString("token", "change-me"),
                Math.max(1, config.getInt("reconnect-seconds", 5))
        );
    }
}
