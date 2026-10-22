package ru.stickoroch.mineshop.web.plugin;

import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.stickoroch.mineshop.web.delivery.DeliveryService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * WebSocket-обработчик плагина Minecraft.
 *
 * Протокол:
 * <pre>
 * plugin -> backend: hello {token, serverId}
 * backend -> plugin: hello_ok {serverId}
 * plugin -> backend: sync
 * backend -> plugin: pending {orders:[...]}
 * plugin -> backend: player_online {player}
 * backend -> plugin: pending {orders:[...]}   (только для этого игрока)
 * backend -> plugin: deliver {orderId, player, command, amount}
 * plugin -> backend: delivery_result {orderId, success, error}
 * backend -> plugin: ping
 * plugin -> backend: pong
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class PluginWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginWebSocketHandler.class);
    private static final String SESSION_ATTRIBUTE = "mineshop.plugin.session";

    private final JsonMapper jsonMapper;
    private final PluginSessionRegistry sessions;
    private final DeliveryService deliveryService;
    private final PluginProperties properties;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOGGER.info("New plugin WebSocket connection: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        PluginMessage pluginMessage;

        try {
            pluginMessage = jsonMapper.readValue(message.getPayload(), PluginMessage.class);
        } catch (Exception e) {
            LOGGER.warn("Invalid plugin message from {}: {}", session.getId(), e.getMessage());
            close(session, "Invalid message");
            return;
        }

        PluginSession pluginSession = (PluginSession) session.getAttributes().get(SESSION_ATTRIBUTE);

        if (pluginSession == null) {
            authenticate(session, pluginMessage);
            return;
        }

        pluginSession.touch();

        try {
            switch (pluginMessage.type() == null ? "" : pluginMessage.type()) {
                case PluginMessage.TYPE_SYNC -> deliveryService.sendPending(pluginSession);
                case PluginMessage.TYPE_PLAYER_ONLINE ->
                        deliveryService.sendPendingForPlayer(pluginSession, pluginMessage.player());
                case PluginMessage.TYPE_DELIVERY_RESULT -> handleDeliveryResult(pluginMessage);
                case PluginMessage.TYPE_PONG -> {
                    // touch() выше уже отметил сессию живой
                }
                case PluginMessage.TYPE_PING -> pluginSession.send(PluginMessage.ping());
                default -> pluginSession.send(PluginMessage.error("Unknown message type: " + pluginMessage.type()));
            }
        } catch (Exception e) {
            LOGGER.warn("Could not process plugin message {}: {}", pluginMessage.type(), e.getMessage());
        }
    }

    private void authenticate(WebSocketSession session, PluginMessage message) {
        if (!PluginMessage.TYPE_HELLO.equals(message.type())) {
            close(session, "Authenticate first");
            return;
        }

        if (!tokenMatches(message.token())) {
            LOGGER.warn("Plugin authentication failed for session {}", session.getId());
            close(session, "Invalid token");
            return;
        }

        String serverId = message.serverId() == null || message.serverId().isBlank()
                ? "default"
                : message.serverId().trim();

        PluginSession pluginSession = new PluginSession(session, jsonMapper, serverId);
        session.getAttributes().put(SESSION_ATTRIBUTE, pluginSession);
        sessions.register(pluginSession);

        try {
            pluginSession.send(PluginMessage.helloOk(serverId));
        } catch (Exception e) {
            LOGGER.warn("Could not confirm plugin connection: {}", e.getMessage());
            close(session, "Handshake failed");
        }
    }

    private void handleDeliveryResult(PluginMessage message) {
        if (message.orderId() == null) {
            LOGGER.warn("Plugin sent delivery_result without orderId");
            return;
        }

        deliveryService.handleResult(
                message.orderId(),
                Boolean.TRUE.equals(message.success()),
                message.error()
        );
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        PluginSession pluginSession = (PluginSession) session.getAttributes().get(SESSION_ATTRIBUTE);

        if (pluginSession != null) {
            sessions.remove(pluginSession);
            LOGGER.info("Plugin server {} disconnected: {}", pluginSession.getServerId(), status);

            if (!sessions.hasActive()) {
                deliveryService.releaseInProgress();
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        LOGGER.warn("Plugin WebSocket transport error: {}", exception.getMessage());
        close(session, "Transport error");
    }

    private boolean tokenMatches(String provided) {
        if (provided == null || properties.getToken() == null) {
            return false;
        }

        return MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                properties.getToken().getBytes(StandardCharsets.UTF_8)
        );
    }

    private void close(WebSocketSession session, String reason) {
        try {
            if (session.isOpen()) {
                session.close(CloseStatus.POLICY_VIOLATION.withReason(reason));
            }
        } catch (Exception e) {
            LOGGER.debug("Could not close plugin session {}: {}", session.getId(), e.getMessage());
        }
    }
}

