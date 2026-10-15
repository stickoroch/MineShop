package ru.stickoroch.mineshop.core;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class PlatformUserId {

    @Column(name = "platform_id", nullable = false)
    private Long platformId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private PlatformType platform;



    public PlatformUserId(long platformId, @NonNull String platform) {
        this.platform = PlatformType.valueOf(platform.toUpperCase());
        this.platformId = platformId;
    }

    public static PlatformUserId fromString(String id) {
        String[] parts = id.split("@");
        return new PlatformUserId(Long.parseLong(parts[1]), PlatformType.valueOf(parts[0].toUpperCase()));
    }

    @Override
    public String toString() {
        return platform.toString() + "@" + platformId;
    }
}
