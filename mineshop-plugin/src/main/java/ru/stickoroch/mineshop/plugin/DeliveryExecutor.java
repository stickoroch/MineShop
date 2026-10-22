package ru.stickoroch.mineshop.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeliveryExecutor {

    private final MineShopPlugin plugin;
    private final BackendWebSocketClient client;
    private final Set<UUID> running = ConcurrentHashMap.newKeySet();

    public DeliveryExecutor(MineShopPlugin plugin, BackendWebSocketClient client) {
        this.plugin = plugin;
        this.client = client;
    }

    public void execute(DeliveryTask task) {
        if (task == null || task.orderId() == null) {
            return;
        }

        if (!running.add(task.orderId())) {
            plugin.getLogger().info("Order " + task.orderId() + " is already being processed");
            return;
        }

        try {
            Bukkit.getScheduler().runTask(plugin, () -> executeOnMainThread(task));
        } catch (Exception e) {
            finish(task, false, "SCHEDULER_ERROR");
        }
    }

    private void executeOnMainThread(DeliveryTask task) {
        try {
            Player player = Bukkit.getPlayerExact(task.player());

            if (player == null || !player.isOnline()) {
                finish(task, false, "PLAYER_OFFLINE");
                return;
            }

            String command = task.command();

            if (command == null || command.isBlank()) {
                finish(task, false, "EMPTY_COMMAND");
                return;
            }

            command = command
                    .replace("%player%", player.getName())
                    .replace("%amount%", Integer.toString(task.amount()));

            if (command.startsWith("/")) {
                command = command.substring(1);
            }

            plugin.getLogger().info("Delivering order " + task.orderId() + " to " + player.getName()
                    + " via command: " + command);

            boolean success;

            try {
                success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            } catch (Exception e) {
                plugin.getLogger().warning("Command failed for order " + task.orderId() + ": " + e.getMessage());
                finish(task, false, "COMMAND_ERROR");
                return;
            }

            finish(task, success, success ? null : "COMMAND_FAILED");
        } catch (Exception e) {
            plugin.getLogger().warning("Delivery failed for order " + task.orderId() + ": " + e.getMessage());
            finish(task, false, "DELIVERY_ERROR");
        }
    }

    private void finish(DeliveryTask task, boolean success, String error) {
        running.remove(task.orderId());
        client.sendResult(task.orderId(), success, error);

        if (success) {
            plugin.getLogger().info("Order " + task.orderId() + " delivered");
        } else {
            plugin.getLogger().warning("Order " + task.orderId() + " was not delivered: " + error);
        }
    }
}
