package ru.urfu.weatherforecastbot.util;

import org.springframework.stereotype.Component;
import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WeatherForecastFormatterImpl implements WeatherForecastFormatter {

    /**
     * Форматировщик даты и времени
     */
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH-mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public String formatTodayForecast(List<WeatherForecast> forecasts) {
        StringBuilder sb = new StringBuilder("\uD83C\uDF21️ Прогноз погоды на сегодня:\n\n");
        sb.append(formatWeatherForecasts(forecasts));
        return sb.toString();
    }

    @Override
    public String formatWeekForecast(List<WeatherForecast> forecasts) {
        Map<LocalDate, List<WeatherForecast>> forecastsByDate = forecasts.stream()
                .collect(Collectors.groupingBy(weatherForecast -> weatherForecast.dateTime().toLocalDate()));

        List<LocalDate> sortedDates = new ArrayList<>(forecastsByDate.keySet());
        Collections.sort(sortedDates);

        StringBuilder sb = new StringBuilder("\uD83C\uDF21️ Прогноз погоды на неделю вперед:");

        for (LocalDate date : sortedDates) {
            sb.append("\n\n").append(dateFormatter.format(date)).append(":\n");

            List<WeatherForecast> dateForecasts = forecastsByDate.get(date);
            String formattedForecasts = formatWeatherForecasts(dateForecasts.stream()
                    .filter(weatherForecast -> weatherForecast.dateTime().getHour() % 4 == 0)
                    .toList());

            sb.append(formattedForecasts);
        }

        return sb.toString();
    }

    /**
     * Форматирует один прогноз погоды и возвращает представление в виде строки
     *
     * @param forecast прогноз погоды
     * @return прогноз погоды в виде строки
     */
    private String formatWeatherForecast(WeatherForecast forecast) {
        StringBuilder sb = new StringBuilder();
        sb.append(dateTimeFormatter.format(forecast.dateTime())).append(": ");
        sb.append(forecast.temperature()).append("°C")
                .append(" (по ощущению ").append(forecast.feelsLikeTemperature()).append("°C)");
        return sb.toString();
    }

    /**
     * Форматирует список прогнозов погоды и возвращает представление в виде строки
     *
     * @param forecasts список прогнозов погоды
     * @return прогнозы погоды в виде строки
     */
    private String formatWeatherForecasts(List<WeatherForecast> forecasts) {
        return forecasts.stream().map(this::formatWeatherForecast).collect(Collectors.joining("\n"));
    }
}
