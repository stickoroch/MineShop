package ru.stickoroch.mineshop.web.dto;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;
import ru.stickoroch.mineshop.core.OrderStatus;
import ru.stickoroch.mineshop.core.model.AdminModel;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.core.model.UserModel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Dtos {

    public record OrderResponse(
            @NonNull UUID paymentId,
            @NonNull Long userId,
            @NonNull String mcName,
            @NonNull Long productId,
            int amount,
            @NonNull BigDecimal cost,
            @NonNull OrderStatus status,
            @NonNull Instant createdAt,
            @Nullable Instant paidAt,
            @Nullable Instant completeAt
    ) {

        public static OrderResponse fromModel(OrderModel model) {
            return new OrderResponse(model.getPaymentId(), model.getUser().getId(),
                    model.getUser().getMcNickname(), model.getProduct().getId(),
                    model.getAmount(), model.getCost(), model.getStatus(), model.getCreatedAt(),
                    model.getPaidAt(), model.getCompleteAt());
        }
    }

    public record OrderSetStatus(@NonNull UUID orderId, @NonNull OrderStatus status) {
    }


    public record ProductResponse(
            @NonNull Long id,
            @NonNull String title,
            @NonNull String description,
            @NonNull BigDecimal price,
            @NonNull String giveCommand,
            @NonNull Boolean isActive
    ) {
        public static ProductResponse fromModel(ProductModel model){
            return new ProductResponse(model.getId(), model.getTitle(),
                    model.getDescription(), model.getPrice(),
                  model.getGiveCommand(), model.getIsActive());
        }
    }

    public record ProductRequest(
            @NonNull String title,
            @NonNull String description,
            @NonNull BigDecimal price,
            @NonNull String giveCommand,
            @NonNull Boolean isActive
    ) {
    }

    public record ProductSetActiveRequest(
            @NonNull Long id,
            @NonNull Boolean isActive
    ) {
    }

    public record UserResponse(
            @NonNull Long id,
            @NonNull String username,
            @NonNull String mcNickname
            ){
        public static UserResponse fromModel(UserModel model){
            return new UserResponse(model.getId(), model.getUsername(), model.getMcNickname());
        }
    }

    public record AdminResponse(@NonNull String username){
        public static AdminResponse fromModel(AdminModel model){
            return new AdminResponse(model.getUsername());
        }
    }

    public record AdminRequest(@NonNull String username, @NonNull String password){
    }


}
