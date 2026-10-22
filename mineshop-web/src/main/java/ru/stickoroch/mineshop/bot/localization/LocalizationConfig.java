package ru.stickoroch.mineshop.bot.localization;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("localization")
public class LocalizationConfig {

    private Map<LocalizationKey, String> text = new HashMap<>();

}
