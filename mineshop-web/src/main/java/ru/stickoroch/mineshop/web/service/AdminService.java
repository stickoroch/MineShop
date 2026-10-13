package ru.stickoroch.mineshop.web.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.stickoroch.mineshop.core.model.AdminModel;
import ru.stickoroch.mineshop.core.repository.AdminRepository;
import ru.stickoroch.mineshop.web.dto.Dtos;
import ru.stickoroch.mineshop.web.security.TokenService;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class AdminService {

    AdminRepository adminRepository;
    TokenService tokenService;


    @Transactional(readOnly = true)
    public List<AdminModel> list(){
        return adminRepository.findAll();
    }

    public AdminModel create(Dtos.AdminRequest r){
        if (adminRepository.existsById(r.username()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Admin already exists");

        AdminModel admin = new AdminModel();
        admin.setUsername(r.username());
        admin.setPasswordHash(tokenService.hashPassword(r.password()));
        admin.setActive(true);
        return adminRepository.save(admin);
    }

    public void delete(String username){
        adminRepository.deleteById(username);
    }
}
