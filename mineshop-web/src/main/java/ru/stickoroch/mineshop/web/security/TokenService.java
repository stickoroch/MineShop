package ru.stickoroch.mineshop.web.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stickoroch.mineshop.core.model.AdminModel;
import ru.stickoroch.mineshop.web.admin.AdminSessionModel;
import ru.stickoroch.mineshop.web.admin.AdminSessionRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class TokenService {
    private final ru.stickoroch.mineshop.web.admin.AdminSessionRepository sessions;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;
    private final Duration ttl;

    public TokenService(AdminSessionRepository sessions, PasswordEncoder passwordEncoder,
                        @Value("${app.admin.auth.token-ttl-hours:12}") long ttlHours) {
        this.sessions = sessions;
        this.passwordEncoder = passwordEncoder;
        this.ttl = Duration.ofHours(ttlHours);
        this.secureRandom = new SecureRandom();
    }

    @Transactional
    public String issue(AdminModel admin) {
        Instant now = Instant.now();

        sessions.revokeAllByAdmin(admin);

        byte[] tokenBytes = new byte[48];
        secureRandom.nextBytes(tokenBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);

        AdminSessionModel session = new AdminSessionModel();
        session.setAdmin(admin);
        session.setTokenHash(hash(token));
        session.setCreatedAt(now);
        session.setExpiresAt(now.plus(ttl));

        sessions.save(session);

        return token;
    }

    @Transactional(readOnly = true)
    public Optional<String> authenticate(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        return sessions.findByTokenHash(hash(token))
                .filter(AdminSessionModel::isActive)
                .map(s -> s.getAdmin().getUsername());
    }

    @Transactional
    public void revoke(String token) {
        sessions.deleteByTokenHash(hash(token));
    }

    public boolean passwordMatches(String raw, String hash) {
        return passwordEncoder.matches(raw, hash);
    }

    public String hashPassword(String raw) {
        return passwordEncoder.encode(raw);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash token", e);
        }
    }
}
