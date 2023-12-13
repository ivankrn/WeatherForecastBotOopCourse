package ru.urfu.weatherforecastbot.util;

import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.util.List;

/**
 * Форматировщик прогноза погоды в удобочитаемый вид
 */
public interface WeatherForecastFormatter {

    /**
     * Форматирует прогнозы погоды на указанный временной период
     *
     * @param timePeriod временной период
     * @param forecasts  список прогнозов погоды
     * @return прогнозы погоды в виде строки
     * @throws IllegalArgumentException если список прогнозов погоды пуст или содержит прогнозы с разными местами
     */
    String formatForecasts(ForecastTimePeriod timePeriod, List<WeatherForecast> forecasts)
            throws IllegalArgumentException;

}
