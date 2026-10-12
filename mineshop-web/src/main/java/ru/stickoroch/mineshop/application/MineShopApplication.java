package ru.stickoroch.mineshop.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.stickoroch.mineshop.bot.localization.LocalizationConfig;

@SpringBootApplication(scanBasePackages = "ru.stickoroch.mineshop")
@EnableJpaRepositories(basePackages = {"ru.stickoroch.mineshop"})
@EntityScan(basePackages = {"ru.stickoroch.mineshop"})
@EnableConfigurationProperties({LocalizationConfig.class})
@EnableScheduling
public class MineShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(MineShopApplication.class, args);
    }

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger("MineShop");
    }
}
