package ru.stickoroch.mineshop.bot.yooKassa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.deelter.yookassa.YooKassa;

@Configuration
public class YooKassaPaymentConfiguration {

    private final int shopId;
    private final String token;

    public YooKassaPaymentConfiguration(
            @Value("${app.yooKassa.shopId}")
            Integer shopId,
            @Value("${app.yooKassa.token}")
            String token
    ) {
        this.shopId = shopId;
        this.token = token;
    }

    @Bean
    public YooKassa getYooKassa() {
        return YooKassa.create(shopId, token);
    }
}