package ru.stickoroch.mineshop.web.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.stickoroch.mineshop.web.dto.Dtos;
import ru.stickoroch.mineshop.web.service.AdminService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminController {

    AdminService adminService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }

    @GetMapping("/admins")
    public List<Dtos.AdminResponse> getAdmins() {
        return adminService.list().stream().map(Dtos.AdminResponse::fromModel).toList();
    }

    @PutMapping("/create")
    public Dtos.AdminResponse createAdmin(@RequestBody Dtos.AdminRequest r) {
        return Dtos.AdminResponse.fromModel(adminService.create(r));
    }

    @DeleteMapping("/delete/{username}")
    public void delete(@PathVariable String username) {
        adminService.delete(username);
    }
}
