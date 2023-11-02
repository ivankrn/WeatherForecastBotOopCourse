package ru.urfu.weatherforecastbot.util;

import org.springframework.stereotype.Component;
import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WeatherForecastFormatterImpl implements WeatherForecastFormatter {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH-mm");

    @Override
    public String formatTodayForecast(List<WeatherForecast> forecasts) {
        StringBuilder sb = new StringBuilder("\uD83C\uDF21️ Прогноз погоды на сегодня:\n\n");
        sb.append(formatWeatherForecasts(forecasts));
        return sb.toString();
    }

    private String formatWeatherForecast(WeatherForecast forecast) {
        StringBuilder sb = new StringBuilder();
        sb.append(dateTimeFormatter.format(forecast.dateTime())).append(": ");
        sb.append(forecast.temperature()).append("°C")
                .append(" (по ощущению ").append(forecast.feelsLikeTemperature()).append("°C)");
        return sb.toString();
    }

    private String formatWeatherForecasts(List<WeatherForecast> forecasts) {
        return forecasts.stream().map(this::formatWeatherForecast).collect(Collectors.joining("\n"));
    }

}
