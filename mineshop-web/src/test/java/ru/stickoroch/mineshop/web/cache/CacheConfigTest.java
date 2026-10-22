package ru.stickoroch.mineshop.web.cache;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    @Test
    void registersApplicationCaches() {
        CacheManager cacheManager = new CacheConfig().cacheManager(
                1000, 5,
                1000, 10,
                500, 60,
                200, 60
        );

        assertThat(cacheManager.getCache(CacheNames.MINECRAFT_PROFILES)).isNotNull();
        assertThat(cacheManager.getCache(CacheNames.PRODUCT_SEARCH)).isNotNull();
        assertThat(cacheManager.getCache(CacheNames.PRODUCT_CATALOG)).isNotNull();
    }
}
