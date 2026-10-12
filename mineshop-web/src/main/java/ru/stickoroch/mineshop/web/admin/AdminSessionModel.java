package ru.stickoroch.mineshop.web.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.stickoroch.mineshop.core.model.AdminModel;

import java.time.Instant;

@Entity
@Table(name = "admin_sessions", indexes = @Index(name = "idx_admin_sessions_token_hash", columnList = "tokenHash", unique = true))
@Getter
@Setter
@NoArgsConstructor
public class AdminSessionModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private AdminModel admin;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(Instant.now()) && admin.isActive();
    }
}
