package ru.urfu.weatherforecastbot.service;

import ru.urfu.weatherforecastbot.util.ForecastTimePeriod;

/**
 * Обработчик запросов на получение прогнозов погоды
 */
public interface WeatherForecastRequestHandler {

    /**
     * Обрабатывает запрос на получение прогноза погоды в указанном месте и временном периоде и возвращает прогнозы
     * погоды в виде строки
     *
     * @param placeName  место прогноза
     * @param timePeriod временной период прогноза
     * @return прогнозы погоды в виде строки
     */
    String handleForecasts(String placeName, ForecastTimePeriod timePeriod);

}
