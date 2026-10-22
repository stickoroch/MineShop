package ru.stickoroch.mineshop.core.model;

import jakarta.persistence.*;
import lombok.*;
import ru.stickoroch.mineshop.core.DeliveryStatus;

import java.time.Instant;

/**
 * Очередь выдачи оплаченных заказов.
 *
 * Заказ и его выдача намеренно разделены: {@link OrderModel#getStatus()} отражает
 * бизнес-статус покупки, а эта таблица — техническое состояние доставки
 * (попытки, ошибки, момент выдачи). Благодаря этому оплаченный заказ не теряется,
 * если Minecraft-сервер выключен или игрок не в сети.
 */
@Entity
@Table(
        name = "order_deliveries",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_delivery_order", columnNames = "order_id"),
        indexes = {
                @Index(name = "idx_order_delivery_status", columnList = "status"),
                @Index(name = "idx_order_delivery_player", columnList = "player_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDeliveryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderModel order;

    /**
     * Ник игрока на момент оплаты. Хранится снимком, чтобы выдача
     * не зависела от последующей смены ника в профиле.
     */
    @Column(name = "player_name", nullable = false, length = 32)
    private String playerName;

    /**
     * Снимок команды выдачи на момент оплаты.
     */
    @Column(name = "command", nullable = false, columnDefinition = "TEXT")
    private String command;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(name = "last_error", length = 255)
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
