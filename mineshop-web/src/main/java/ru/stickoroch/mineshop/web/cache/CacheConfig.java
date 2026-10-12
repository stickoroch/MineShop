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
    CacheManager cacheManager(@Value("${app.cache.admin.maximum-size:10000}") long maximumSize,
                              @Value("${app.cache.admin.expire-after-write-minutes:5}") long expireMinutes) {
        CaffeineCacheManager manager = new CaffeineCacheManager(/*todo*/);
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(maximumSize)
                .expireAfterWrite(Duration.ofMinutes(expireMinutes)));
        return manager;
    }
}
