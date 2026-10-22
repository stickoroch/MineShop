package ru.stickoroch.mineshop.web.plugin;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

/**
 * Сообщение WebSocket-протокола между бэкендом и плагином.
 * Все поля, кроме {@code type}, опциональны и зависят от типа сообщения.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PluginMessage(
        String type,
        String token,
        String serverId,
        UUID orderId,
        String player,
        String command,
        Integer amount,
        Boolean success,
        String error,
        List<DeliveryTask> orders
) {

    public static final String TYPE_HELLO = "hello";
    public static final String TYPE_HELLO_OK = "hello_ok";
    public static final String TYPE_SYNC = "sync";
    public static final String TYPE_PENDING = "pending";
    public static final String TYPE_DELIVER = "deliver";
    public static final String TYPE_DELIVERY_RESULT = "delivery_result";
    public static final String TYPE_PLAYER_ONLINE = "player_online";
    public static final String TYPE_PING = "ping";
    public static final String TYPE_PONG = "pong";

    public static PluginMessage helloOk(String serverId) {
        return new PluginMessage(TYPE_HELLO_OK, null, serverId, null, null, null, null, null, null, null);
    }

    public static PluginMessage pending(List<DeliveryTask> tasks) {
        return new PluginMessage(TYPE_PENDING, null, null, null, null, null, null, null, null, tasks);
    }

    public static PluginMessage deliver(DeliveryTask task) {
        return new PluginMessage(
                TYPE_DELIVER,
                null,
                null,
                task.orderId(),
                task.player(),
                task.command(),
                task.amount(),
                null,
                null,
                null
        );
    }

    public static PluginMessage ping() {
        return new PluginMessage(TYPE_PING, null, null, null, null, null, null, null, null, null);
    }

    public static PluginMessage error(String message) {
        return new PluginMessage("error", null, null, null, null, null, null, false, message, null);
    }
}
