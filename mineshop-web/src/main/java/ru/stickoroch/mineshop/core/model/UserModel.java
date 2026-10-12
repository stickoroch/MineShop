package ru.stickoroch.mineshop.core.model;

import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_tg_id", columnList = "tg_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserModel {

    @Id
    @Column(name = "tg_id", nullable = false, unique = true)
    private Long tgId;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "mc_nickname", length = 32)
    private String mcNickname;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderModel> orders = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public void addOrder(OrderModel order) {
        orders.add(order);
        order.setUser(this);
    }

    public void removeOrder(OrderModel order) {
        orders.remove(order);
        order.setUser(null);
    }
}