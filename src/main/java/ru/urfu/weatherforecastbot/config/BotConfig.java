package ru.urfu.weatherforecastbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Конфигурация бота
 */
@Component
public class BotConfig {

    /**
     * Название бота
     */
    private final String name;
    /**
     * Токен бота
     */
    private final String token;

    public BotConfig(@Value("${bot.name}") String name, @Value("${bot.token}") String token) {
        this.name = name;
        this.token = token;
    }

    /**
     * Возвращает название бота
     *
     * @return название бота
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает токен бота
     *
     * @return токен бота
     */
    public String getToken() {
        return token;
    }

}
