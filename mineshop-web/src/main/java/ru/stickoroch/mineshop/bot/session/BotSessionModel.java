package ru.stickoroch.mineshop.bot.session;

import jakarta.persistence.*;
import lombok.*;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.UserModel;

import java.time.Instant;

@Entity
@Table(name = "bot_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotSessionModel {

    @Id
    @Column(name = "user_id")
    private PlatformUserId userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BotState state;

    private Long productId;

    private Long lastPaymentId;

    @Column(name = "search_query", length = 128)
    private String searchQuery;

    private int lastMessageId;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void updateTimestamp() {
        updatedAt = Instant.now();
    }

   @Builder.Default
   private int page = 0;
}