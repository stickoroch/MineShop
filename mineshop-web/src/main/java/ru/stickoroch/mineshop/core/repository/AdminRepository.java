package ru.stickoroch.mineshop.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.stickoroch.mineshop.core.model.AdminModel;

@Repository
public interface AdminRepository extends JpaRepository<AdminModel, String> {


}
