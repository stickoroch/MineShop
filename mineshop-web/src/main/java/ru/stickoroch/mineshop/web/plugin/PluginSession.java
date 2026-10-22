package ru.stickoroch.mineshop.web.plugin;

import tools.jackson.databind.json.JsonMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Обёртка над WebSocket-сессией плагина.
 * Отправка синхронизирована, потому что WebSocketSession не потокобезопасна.
 */
@Getter
public class PluginSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginSession.class);

    private final WebSocketSession session;
    private final JsonMapper jsonMapper;
    private final String serverId;

    private volatile Instant lastSeen = Instant.now();

    public PluginSession(WebSocketSession session, JsonMapper jsonMapper, String serverId) {
        this.session = session;
        this.jsonMapper = jsonMapper;
        this.serverId = serverId;
    }

    public synchronized void send(PluginMessage message) throws IOException {
        if (!session.isOpen()) {
            throw new IOException("WebSocket-сессия плагина закрыта");
        }

        session.sendMessage(new TextMessage(jsonMapper.writeValueAsString(message)));
    }

    public void touch() {
        lastSeen = Instant.now();
    }

    public boolean isOpen() {
        return session.isOpen();
    }

    public boolean isStale(Duration timeout) {
        return lastSeen.plus(timeout).isBefore(Instant.now());
    }

    public void close(String reason) {
        try {
            if (session.isOpen()) {
                session.close(new CloseStatus(1000, reason));
            }
        } catch (IOException e) {
            LOGGER.debug("Could not close plugin session {}: {}", serverId, e.getMessage());
        }
    }
}

