package ru.stickoroch.mineshop.plugin;

import java.util.UUID;

public record DeliveryTask(
        UUID orderId,
        String player,
        String command,
        int amount
) {
}
