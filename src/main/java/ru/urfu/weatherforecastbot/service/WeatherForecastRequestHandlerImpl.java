package ru.urfu.weatherforecastbot.service;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.util.ForecastTimePeriod;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.util.List;

public class WeatherForecastRequestHandlerImpl implements WeatherForecastRequestHandler {

    /**
     * Сервис для получения прогнозов погоды
     */
    private final WeatherForecastService weatherService;
    /**
     * Форматировщик прогнозов погоды
     */
    private final WeatherForecastFormatter forecastFormatter;

    /**
     * Создает экземпляр {@link WeatherForecastRequestHandlerImpl}, используя в качестве
     * {@link WeatherForecastFormatter} {@link ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl}
     *
     * @param weatherService    сервис для получения прогнозов погоды
     */
    public WeatherForecastRequestHandlerImpl(WeatherForecastService weatherService) {
        this.weatherService = weatherService;
        this.forecastFormatter = new WeatherForecastFormatterImpl();
    }

    /**
     * Создает экземпляр {@link WeatherForecastRequestHandlerImpl}, используя переданные аргументы
     *
     * @param weatherService    сервис для получения прогнозов погоды
     * @param forecastFormatter форматировщик прогнозов погоды
     */
    public WeatherForecastRequestHandlerImpl(WeatherForecastService weatherService,
                                             WeatherForecastFormatter forecastFormatter) {
        this.weatherService = weatherService;
        this.forecastFormatter = forecastFormatter;
    }

    @Override
    public String handleForecasts(String placeName, ForecastTimePeriod timePeriod) {
        List<WeatherForecast> forecasts;
        switch (timePeriod) {
            case TODAY -> forecasts = weatherService.getForecast(placeName, 1);
            case TOMORROW -> forecasts = weatherService.getForecast(placeName, 2);
            case WEEK -> forecasts = weatherService.getForecast(placeName, 7);
            default -> forecasts = List.of();
        }
        if (forecasts.isEmpty()) {
            return BotConstants.NOT_FOUND_PLACE;
        }
        if (timePeriod == ForecastTimePeriod.TOMORROW) {
            forecasts = forecasts.subList(24, 48);
        }
        return forecastFormatter.formatForecasts(timePeriod, forecasts);
    }

}
