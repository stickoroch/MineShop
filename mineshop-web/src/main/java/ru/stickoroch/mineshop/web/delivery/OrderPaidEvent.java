package ru.stickoroch.mineshop.web.delivery;

import java.util.UUID;

/**
 * Событие «заказ оплачен». Публикуется внутри транзакции оплаты,
 * а обработчик запускает выдачу только после успешного коммита.
 */
public record OrderPaidEvent(UUID orderId) {
}
