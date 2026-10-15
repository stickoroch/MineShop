package ru.stickoroch.mineshop.web.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stickoroch.mineshop.core.PlatformType;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.model.UserModel;
import ru.stickoroch.mineshop.core.repository.OrderRepository;
import ru.stickoroch.mineshop.core.repository.UserRepository;

@Transactional
@AllArgsConstructor
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public UserModel getUser(PlatformUserId id) {
        return userRepository.findById(id).orElseThrow();
    }

    public UserModel createIfNotExists(PlatformUserId id, String username) {
        return userRepository.findById(id).orElseGet(() -> create(id.getPlatformId(), username, "-", id.getPlatform()));
    }

    public UserModel create(long id, String username, String mcNickname, PlatformType platformType) {
        UserModel userModel = new UserModel();
        userModel.setId(new PlatformUserId(id, platformType));
        userModel.setUsername(username);
        userModel.setMcNickname(mcNickname);

        return userRepository.save(userModel);
    }

    @Transactional(readOnly = true)
    public Page<UserModel> getUsers(int page) {
        return userRepository.findAll(PageRequest.of(page, 10));
    }

    @Transactional(readOnly = true)
    public Page<OrderModel> getOrders(PlatformUserId userId, int page) {
        return orderRepository.findAllByUser_Id(userId, PageRequest.of(page, 10, Sort.by("createdAt").descending()));
    }


    public void setMcName(PlatformUserId platformUserId, String name) {
        UserModel user = getUser(platformUserId);
        user.setMcNickname(name);

        userRepository.save(user);
    }
}
