package ru.urfu.weatherforecastbot.model;

import java.time.LocalDateTime;

/**
 * Прогноз погоды
 *
 * @param dateTime             дата и время прогноза (по местному часовому поясу)
 * @param temperature          температура (в градусах Цельсия)
 * @param feelsLikeTemperature температура по ощущению (в градусах Цельсия)
 */
public record WeatherForecast(LocalDateTime dateTime, double temperature, double feelsLikeTemperature) {
}
