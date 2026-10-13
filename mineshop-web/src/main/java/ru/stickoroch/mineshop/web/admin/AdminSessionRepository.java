package ru.stickoroch.mineshop.web.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.stickoroch.mineshop.core.model.AdminModel;

import java.util.List;
import java.util.Optional;

public interface AdminSessionRepository extends JpaRepository<AdminSessionModel, Long> {
    Optional<AdminSessionModel> findByTokenHash(String tokenHash);


    Optional<AdminSessionModel> findByAdmin_Username(String admin);


    @Modifying
    @Query("""
                delete AdminSessionModel s
                where s.admin = :admin
            """)
    void revokeAllByAdmin(
            @Param("admin") AdminModel admin
    );

    void deleteByTokenHash(String hash);
}
