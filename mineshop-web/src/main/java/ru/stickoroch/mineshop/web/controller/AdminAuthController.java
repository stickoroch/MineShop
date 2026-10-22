package ru.stickoroch.mineshop.web.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.stickoroch.mineshop.core.model.AdminModel;
import ru.stickoroch.mineshop.core.repository.AdminRepository;
import ru.stickoroch.mineshop.web.security.TokenService;


@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminRepository admins;
    private final TokenService tokens;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest r) {
        AdminModel admin = admins.findById(r.username())
                .filter(AdminModel::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!tokens.passwordMatches(r.password(), admin.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return new LoginResponse(tokens.issue(admin), admin.getUsername());
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) tokens.revoke(authorization.substring(7));
    }

    @GetMapping("/me")
    public String me(@AuthenticationPrincipal String principal) {
        return principal;
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(String token, String username) {
    }
}
