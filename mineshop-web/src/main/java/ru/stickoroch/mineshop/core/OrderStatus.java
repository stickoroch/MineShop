package ru.stickoroch.mineshop.core;

public enum OrderStatus {
    PENDING,            // создан, ждёт оплаты
    PAID,               // оплачен ЮKassa
    DELIVERED,          // выдан на сервере
    PENDING_DELIVERY,   // оплачен, но RCON не сработал — выдать вручную
    CANCELED            // отменён
}
