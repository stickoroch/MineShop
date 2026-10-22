package ru.stickoroch.mineshop.plugin;

import com.google.gson.Gson;

import java.util.List;
import java.util.UUID;

public record PluginMessage(
        String type,
        String token,
        UUID orderId,
        String player,
        String command,
        Integer amount,
        Boolean success,
        String error,
        List<DeliveryTask> orders
) {

    public static PluginMessage hello(String token) {
        return new PluginMessage("hello", token, null, null, null, null, null, null, null);
    }

    public static PluginMessage sync() {
        return new PluginMessage("sync", null, null, null, null, null, null, null, null);
    }

    public static PluginMessage playerOnline(String player) {
        return new PluginMessage("player_online", null, null, player, null, null, null, null, null);
    }

    public static PluginMessage pong() {
        return new PluginMessage("pong", null, null, null, null, null, null, null, null);
    }

    public static PluginMessage deliveryResult(UUID orderId, boolean success, String error) {
        return new PluginMessage(
                "delivery_result",
                null,
                orderId,
                null,
                null,
                null,
                success,
                error,
                null
        );
    }

    public String toJson(Gson gson) {
        return gson.toJson(this);
    }
}
