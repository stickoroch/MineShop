package ru.stickoroch.mineshop.web.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.repository.OrderRepository;
import ru.stickoroch.mineshop.web.dto.Dtos;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class OrderService {

    OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public OrderModel getOrder(@NonNull UUID id) {
        return orderRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Page<OrderModel> getOrders(int page) {
        return orderRepository.findAll(PageRequest.of(page, 10, Sort.by("createdAt").descending()));
    }

    @Transactional(readOnly = true)
    public Page<OrderModel> getActiveOrders(int page) {
        return orderRepository.findAllByStatusIn(
                PageRequest.of(page, 10, Sort.by("createdAt").descending()),
                List.of(OrderStatus.PENDING_DELIVERY, OrderStatus.PENDING)
        );
    }

    public void setOrderStatus(Dtos.OrderSetStatus r) {
        OrderModel orderModel = orderRepository.findById(r.orderId()).orElseThrow();
        orderModel.setStatus(r.status());
    }

}
