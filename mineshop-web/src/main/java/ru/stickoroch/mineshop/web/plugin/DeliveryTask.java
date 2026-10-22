package ru.stickoroch.mineshop.web.plugin;

import java.util.UUID;

/**
 * Одна задача выдачи, отправляемая плагину.
 */
public record DeliveryTask(
        UUID orderId,
        String player,
        String command,
        int amount
) {
}
