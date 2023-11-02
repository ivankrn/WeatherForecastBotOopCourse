package ru.urfu.weatherforecastbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация бота
 */
@Configuration
@ConfigurationProperties(prefix = "bot")
public class BotConfig {

    private String name;
    private String token;

    /**
     * Возвращает название бота
     *
     * @return название бота
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название бота
     *
     * @param name название бота
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает токен бота
     *
     * @return токен бота
     */
    public String getToken() {
        return token;
    }

    /**
     * Устанавливает токен бота
     *
     * @param token токен бота
     */
    public void setToken(String token) {
        this.token = token;
    }
}
