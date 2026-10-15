package ru.stickoroch.mineshop.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel, PlatformUserId> {
}
