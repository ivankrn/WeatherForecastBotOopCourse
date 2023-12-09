package ru.urfu.weatherforecastbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Класс для точки входа в приложение
 */
@SpringBootApplication
public class WeatherForecastBotApplication {

    /**
     * Точка входа, запускающая приложение Spring
     *
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(WeatherForecastBotApplication.class, args);
    }

}
