package ru.urfu.weatherforecastbot.service;

import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.util.List;

/**
 * Сервис для получения прогнозов погоды
 */
public interface WeatherForecastService {


    /**
     * Возвращает список прогнозов погоды по часам для указанного числа дней, включая сегодня (при этом дата и время
     * прогнозов указаны по часовому поясу данного места), или пустой список, если место не найдено
     *
     * @param placeName название места
     * @param daysCount количество дней
     * @return список прогнозов погоды по часам для указанного числа дней, или пустой список, если место не найдено
     */
    List<WeatherForecast> getForecast(String placeName, int daysCount);

}
