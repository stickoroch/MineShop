package ru.stickoroch.mineshop.web.admin;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.stickoroch.mineshop.core.model.AdminModel;
import ru.stickoroch.mineshop.core.repository.AdminRepository;
import ru.stickoroch.mineshop.web.security.TokenService;

@Configuration
@RequiredArgsConstructor
public class BootstrapAdminConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapAdminConfig.class);

    private final AdminRepository admins;
    private final TokenService tokens;

    @Bean
    CommandLineRunner bootstrapAdmin(@Value("${app.admin.bootstrap.username:admin}") String username,
                                     @Value("${app.admin.bootstrap.password:change-me-now}") String password) {
        return args -> {
            if (admins.existsById(username)) {
                return;
            }

            if ("change-me-now".equals(password) || "admin".equals(password)) {
                LOGGER.warn("Default administrator password is in use. "
                        + "Set ADMIN_PASSWORD before exposing the panel to the internet.");
            }

            AdminModel admin = new AdminModel();
            admin.setUsername(username);
            admin.setPasswordHash(tokens.hashPassword(password));
            admin.setActive(true);
            admins.save(admin);
        };
    }
}