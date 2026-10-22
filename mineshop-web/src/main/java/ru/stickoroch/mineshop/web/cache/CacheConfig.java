package ru.stickoroch.mineshop.web.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    CacheManager cacheManager(
            @Value("${app.cache.maximum-size:10000}") long maximumSize,
            @Value("${app.cache.default-expire-after-write-minutes:5}") long defaultExpireMinutes,
            @Value("${app.cache.minecraft-profile.maximum-size:10000}") long profileMaximumSize,
            @Value("${app.cache.minecraft-profile.expire-after-write-minutes:10}") long profileExpireMinutes,
            @Value("${app.cache.product-search.maximum-size:2000}") long searchMaximumSize,
            @Value("${app.cache.product-search.expire-after-write-seconds:60}") long searchExpireSeconds,
            @Value("${app.cache.product-catalog.maximum-size:500}") long catalogMaximumSize,
            @Value("${app.cache.product-catalog.expire-after-write-seconds:60}") long catalogExpireSeconds
    ) {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(Duration.ofMinutes(defaultExpireMinutes))
                .recordStats());

        manager.registerCustomCache(CacheNames.MINECRAFT_PROFILES, Caffeine.newBuilder()
                .maximumSize(profileMaximumSize)
                .expireAfterWrite(Duration.ofMinutes(profileExpireMinutes))
                .recordStats()
                .build());

        manager.registerCustomCache(CacheNames.PRODUCT_SEARCH, Caffeine.newBuilder()
                .maximumSize(searchMaximumSize)
                .expireAfterWrite(Duration.ofSeconds(searchExpireSeconds))
                .recordStats()
                .build());

        manager.registerCustomCache(CacheNames.PRODUCT_CATALOG, Caffeine.newBuilder()
                .maximumSize(catalogMaximumSize)
                .expireAfterWrite(Duration.ofSeconds(catalogExpireSeconds))
                .recordStats()
                .build());

        return manager;
    }
}
