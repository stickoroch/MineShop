package ru.stickoroch.mineshop.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final BackendWebSocketClient client;

    public PlayerJoinListener(BackendWebSocketClient client) {
        this.client = client;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (client.isConnected()) {
            client.playerOnline(event.getPlayer().getName());
        }
    }
}
