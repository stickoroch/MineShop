package ru.stickoroch.mineshop.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stickoroch.mineshop.core.DeliveryStatus;
import ru.stickoroch.mineshop.core.model.OrderDeliveryModel;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryQueueRepository extends JpaRepository<OrderDeliveryModel, Long> {

    Optional<OrderDeliveryModel> findByOrder_PaymentId(UUID orderId);

    List<OrderDeliveryModel> findAllByStatusOrderByCreatedAtAsc(DeliveryStatus status);

    List<OrderDeliveryModel> findAllByPlayerNameIgnoreCaseAndStatusOrderByCreatedAtAsc(
            String playerName,
            DeliveryStatus status
    );

    List<OrderDeliveryModel> findAllByStatusAndUpdatedAtBefore(DeliveryStatus status, Instant before);
}
