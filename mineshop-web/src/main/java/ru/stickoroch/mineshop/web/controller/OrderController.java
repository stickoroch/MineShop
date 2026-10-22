package ru.stickoroch.mineshop.web.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.stickoroch.mineshop.web.dto.Dtos;
import ru.stickoroch.mineshop.web.service.OrderService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public Dtos.OrderResponse getOrder(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        return Dtos.OrderResponse.fromModel(orderService.getOrder(uuid));
    }

    @GetMapping("/list/{page}")
    @Transactional(readOnly = true)
    public List<Dtos.OrderResponse> getOrders(@PathVariable int page) {
        return orderService.getOrders(page).map(Dtos.OrderResponse::fromModel).stream().toList();
    }

    @GetMapping("/active/list/{page}")
    @Transactional(readOnly = true)
    public List<Dtos.OrderResponse> getActiveOrders(@PathVariable int page) {
        return orderService.getActiveOrders(page).map(Dtos.OrderResponse::fromModel).stream().toList();
    }

    @PostMapping("/status")
    public void setActiveOrder(@Valid @RequestBody Dtos.OrderSetStatus r) {
        orderService.setOrderStatus(r);
    }

}
