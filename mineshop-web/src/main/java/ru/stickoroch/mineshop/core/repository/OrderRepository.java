package ru.stickoroch.mineshop.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.OrderModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderModel, UUID> {
    Page<OrderModel> findAllByStatusIn(Pageable page, List<OrderStatus> status);

    Page<OrderModel> findAllByUser_Id(PlatformUserId userId, Pageable page);

    Page<OrderModel> findAllByUser_IdAndStatusIn(PlatformUserId userId, List<OrderStatus> statuses, Pageable page);

    Optional<OrderModel> findByPaymentIdAndUser_Id(UUID paymentId, PlatformUserId userId);
}
