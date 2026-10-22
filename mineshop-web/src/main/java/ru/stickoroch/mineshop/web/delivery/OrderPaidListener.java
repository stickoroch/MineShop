package ru.stickoroch.mineshop.web.delivery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * После коммита оплаты отдаёт заказ плагину. Если плагин офлайн,
 * задача просто останется в очереди.
 */
@Component
@RequiredArgsConstructor
public class OrderPaidListener {

    private final DeliveryService deliveryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        deliveryService.dispatchNewOrder(event.orderId());
    }
}
