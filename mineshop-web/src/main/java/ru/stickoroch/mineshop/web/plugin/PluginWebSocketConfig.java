package ru.stickoroch.mineshop.web.plugin;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * Эндпоинт для Minecraft-плагина: ws://host:port/ws/plugin
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class PluginWebSocketConfig implements WebSocketConfigurer {

    private final PluginWebSocketHandler pluginWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(pluginWebSocketHandler, "/ws/plugin")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean pluginWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(256 * 1024);
        container.setMaxBinaryMessageBufferSize(256 * 1024);
        container.setMaxSessionIdleTimeout(0L);
        return container;
    }
}
