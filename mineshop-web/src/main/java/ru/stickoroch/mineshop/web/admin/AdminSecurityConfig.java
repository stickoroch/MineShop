package ru.stickoroch.mineshop.web.admin;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.stickoroch.mineshop.web.security.TokenService;

import java.io.IOException;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {
    private final TokenService tokenService;

    @Bean
    static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/auth/login").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(new AdminTokenFilter(tokenService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private static class AdminTokenFilter extends OncePerRequestFilter {
        private final TokenService tokenService;

        private AdminTokenFilter(TokenService tokenService) {
            this.tokenService = tokenService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
                throws ServletException, IOException {
            String header = request.getHeader(HttpHeaders.AUTHORIZATION);
            String token = null;
            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            } else {
                token = request.getParameter("token");
            }
            if (token != null) {
                tokenService.authenticate(token).ifPresent(principal -> {
                    AbstractAuthenticationToken auth = new AbstractAuthenticationToken(authorities(principal)) {
                        @Override
                        public Object getCredentials() {
                            return null;
                        }

                        @Override
                        public Object getPrincipal() {
                            return principal;
                        }
                    };
                    auth.setAuthenticated(true);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
            chain.doFilter(request, response);
        }

        private List<SimpleGrantedAuthority> authorities(String principal) {
            return List.of(new SimpleGrantedAuthority("ADMIN_" + principal));
        }
    }
}
