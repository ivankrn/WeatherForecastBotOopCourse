package ru.urfu.weatherforecastbot.util;

import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.util.List;

/**
 * Форматировщик прогноза погоды в удобочитаемый вид
 */
public interface WeatherForecastFormatter {

    /**
     * Форматирует прогнозы погоды на сегодня
     *
     * @param forecasts список прогнозов погоды на сегодня
     * @return прогнозы погоды на сегодня в виде строки
     */
    String formatTodayForecast(List<WeatherForecast> forecasts);

    /**
     * Форматирует прогнозы погоды на неделю вперед каждые 4 часа
     *
     * @param forecasts список прогнозов погоды на неделю вперед
     * @return прогнозы погоды на сегодня в виде строки
     */
    String formatWeekForecast(List<WeatherForecast> forecasts);
}
