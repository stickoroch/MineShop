package ru.stickoroch.mineshop.web.minecraft;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.stickoroch.mineshop.web.cache.CacheConfig;
import ru.stickoroch.mineshop.web.cache.CacheNames;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {CacheConfig.class, MinecraftProfileService.class})
@TestPropertySource(properties = {
        "app.minecraft.profile-check.enabled=false",
        "app.minecraft.profile-check.timeout-ms=1000"
})
class MinecraftProfileCacheTest {

    @Autowired
    private MinecraftProfileService profileService;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void cachesProfileLookupsByNormalizedNickname() {
        assertThat(profileService.findProfile("Notch")).isPresent();

        var cache = cacheManager.getCache(CacheNames.MINECRAFT_PROFILES);

        assertThat(cache).isNotNull();
        assertThat(cache.get("notch")).isNotNull();
    }
}
