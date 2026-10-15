package ru.stickoroch.mineshop.bot.localization;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LocalizationService {

    LocalizationConfig config;

    @NonNull
    public String getText(@NonNull LocalizationKey key) {
        return config.getText().getOrDefault(key, key.toString());
    }

    @NonNull
    public String getText(@NonNull LocalizationKey key, Object... replaces) {
        String text = getText(key);
        try {
            return String.format(text, replaces);
        } catch (Exception e) {
            return text;
        }
    }
    public String escapeMarkdown(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("`", "\\`")
                .replace("[", "\\[");
    }
}
