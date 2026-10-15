package ru.stickoroch.mineshop.web.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stickoroch.mineshop.core.PlatformType;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.web.dto.Dtos;
import ru.stickoroch.mineshop.web.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }


    @GetMapping("/{id}")
    public Dtos.UserResponse getUser(@PathVariable String id) {
        return Dtos.UserResponse.fromModel(userService.getUser(PlatformUserId.fromString(id)));
    }

    @GetMapping("/history/{id}/{page}")
    public List<Dtos.OrderResponse> history(@PathVariable String id, @PathVariable int page) {
        PlatformUserId platformUserId = PlatformUserId.fromString(id);
        return userService.getOrders(platformUserId, page).map(Dtos.OrderResponse::fromModel).stream().toList();
    }

    @GetMapping("/list/{page}")
    public List<Dtos.UserResponse> getUsers(@PathVariable int page) {
        return userService.getUsers(page).map(Dtos.UserResponse::fromModel).stream().toList();
    }
}
