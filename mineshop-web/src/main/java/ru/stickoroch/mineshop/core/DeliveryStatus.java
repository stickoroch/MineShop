package ru.stickoroch.mineshop.core;

/**
 * Состояние выдачи оплаченного заказа на Minecraft-сервере.
 */
public enum DeliveryStatus {
    PENDING,        // ждёт отправки на плагин
    IN_PROGRESS,    // отправлено плагину, ждём результат
    DELIVERED,      // плагин подтвердил выдачу
    FAILED          // исчерпаны попытки, нужна ручная выдача
}
