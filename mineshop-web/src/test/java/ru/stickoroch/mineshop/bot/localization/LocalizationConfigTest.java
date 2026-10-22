package ru.stickoroch.mineshop.bot.localization;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizationConfigTest {

    @Test
    void everyLocalizationKeyHasText() throws Exception {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
                .load("localization", new ClassPathResource("localization.yml"));

        assertThat(sources).isNotEmpty();

        LocalizationConfig config = new Binder(ConfigurationPropertySources.from(sources))
                .bind("localization", Bindable.of(LocalizationConfig.class))
                .orElseThrow(() -> new AssertionError("localization.yml не привязался к LocalizationConfig"));

        assertThat(config.getText()).containsKeys(LocalizationKey.values());
        assertThat(config.getText().values()).allSatisfy(text ->
                assertThat(text).isNotBlank());
    }
}
