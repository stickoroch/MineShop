package ru.stickoroch.mineshop.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public class MineShopPlugin extends JavaPlugin {

    private BackendWebSocketClient client;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginSettings settings = PluginSettings.from(getConfig());

        if ("change-me".equals(settings.token())) {
            getLogger().warning("Plugin token is still the default value! Set app.plugin.token on the backend "
                    + "and token in plugins/MineShop/config.yml to the same secret.");
        }

        client = new BackendWebSocketClient(this, settings);
        DeliveryExecutor deliveryExecutor = new DeliveryExecutor(this, client);
        client.setDeliveryExecutor(deliveryExecutor);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(client), this);

        client.start();

        getLogger().info("MineShop enabled");
    }

    @Override
    public void onDisable() {
        if (client != null) {
            client.stop();
        }

        getLogger().info("MineShop disabled");
    }
}
