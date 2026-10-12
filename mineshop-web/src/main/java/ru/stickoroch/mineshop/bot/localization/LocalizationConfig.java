package ru.stickoroch.mineshop.bot.localization;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@PropertySource("classpath:localization.yml")
@ConfigurationProperties("localization")
public class LocalizationConfig {

    private Map<LocalizationKey, String> text = new HashMap<>();

}
