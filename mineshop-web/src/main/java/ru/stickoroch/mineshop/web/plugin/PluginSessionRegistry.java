package ru.stickoroch.mineshop.web.plugin;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр подключённых Minecraft-серверов.
 *
 * Сейчас поддерживается один активный сервер на заказ (у заказа нет serverId),
 * но реестр хранит подключения по идентификатору сервера и вытесняет
 * предыдущее подключение с тем же id.
 */
@Component
@RequiredArgsConstructor
public class PluginSessionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginSessionRegistry.class);

    private final Map<String, PluginSession> sessions = new ConcurrentHashMap<>();
    private final PluginProperties properties;

    public void register(PluginSession pluginSession) {
        PluginSession previous = sessions.put(pluginSession.getServerId(), pluginSession);

        if (previous != null && previous != pluginSession) {
            LOGGER.warn("Plugin server {} reconnected, closing previous session", pluginSession.getServerId());
            previous.close("Replaced by new connection");
        }

        LOGGER.info("Plugin server {} connected", pluginSession.getServerId());
    }

    public void remove(PluginSession pluginSession) {
        sessions.remove(pluginSession.getServerId(), pluginSession);
    }

    public Optional<PluginSession> getActive() {
        return sessions.values().stream()
                .filter(PluginSession::isOpen)
                .findFirst();
    }

    public boolean hasActive() {
        return sessions.values().stream().anyMatch(PluginSession::isOpen);
    }

    public Collection<PluginSession> all() {
        return sessions.values();
    }

    @Scheduled(fixedDelay = 30_000)
    public void keepAlive() {
        Duration timeout = Duration.ofSeconds(properties.getSessionTimeoutSeconds());

        sessions.values().forEach(pluginSession -> {
            if (!pluginSession.isOpen()) {
                remove(pluginSession);
                return;
            }

            if (pluginSession.isStale(timeout)) {
                LOGGER.warn("Plugin server {} did not respond, closing session", pluginSession.getServerId());
                pluginSession.close("Ping timeout");
                remove(pluginSession);
                return;
            }

            try {
                pluginSession.send(PluginMessage.ping());
            } catch (Exception e) {
                LOGGER.warn("Could not ping plugin server {}: {}", pluginSession.getServerId(), e.getMessage());
                pluginSession.close("Ping failed");
                remove(pluginSession);
            }
        });
    }
}
