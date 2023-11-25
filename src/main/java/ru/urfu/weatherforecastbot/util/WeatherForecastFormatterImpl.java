package ru.urfu.weatherforecastbot.util;

import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WeatherForecastFormatterImpl implements WeatherForecastFormatter {

    /**
     * Форматировщик времени для форматирования по часам
     */
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH-mm");
    /**
     * Форматировщик даты для форматирования на неделю
     */
    private final DateTimeFormatter weekDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    /**
     * Сообщение исключения при пустом списке прогнозов
     */
    private final String EMPTY_FORECASTS_EXCEPTION_MESSAGE = "Forecasts are empty!";
    /**
     * Сообщение исключения при прогнозах, не относящихся к одному месту
     */
    private final String FORECASTS_WITH_DIFFERENT_PLACES_EXCEPTION_MESSAGE = "Forecasts have different places!";

    @Override
    public String formatTodayForecast(List<WeatherForecast> forecasts) throws IllegalArgumentException {
        if (forecasts.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_FORECASTS_EXCEPTION_MESSAGE);
        }
        if (hasDifferentPlaces(forecasts)) {
            throw new IllegalArgumentException(FORECASTS_WITH_DIFFERENT_PLACES_EXCEPTION_MESSAGE);
        }
        String placeName = forecasts.get(0).place().name();
        return "\uD83C\uDF21️ Прогноз погоды на сегодня (%s):\n\n%s"
                .formatted(placeName, formatWeatherForecasts(forecasts));
    }

    @Override
    public String formatWeekForecast(List<WeatherForecast> forecasts) throws IllegalArgumentException {
        if (forecasts.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_FORECASTS_EXCEPTION_MESSAGE);
        }
        if (hasDifferentPlaces(forecasts)) {
            throw new IllegalArgumentException(FORECASTS_WITH_DIFFERENT_PLACES_EXCEPTION_MESSAGE);
        }
        Map<LocalDate, List<WeatherForecast>> forecastsByDate = forecasts.stream()
                .collect(Collectors.groupingBy(weatherForecast -> weatherForecast.dateTime().toLocalDate()));

        List<LocalDate> sortedDates = new ArrayList<>(forecastsByDate.keySet());
        Collections.sort(sortedDates);

        int hourInterval = 4;
        String placeName = forecasts.get(0).place().name();
        StringBuilder sb = new StringBuilder("\uD83C\uDF21️ Прогноз погоды на неделю вперед (%s):".formatted(placeName));

        for (LocalDate date : sortedDates) {
            sb.append("\n\n").append(weekDateFormatter.format(date)).append(":\n");

            List<WeatherForecast> dateForecasts = forecastsByDate.get(date);
            String formattedForecasts = formatWeatherForecasts(dateForecasts.stream()
                    .filter(weatherForecast -> weatherForecast.dateTime().getHour() % hourInterval == 0)
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
        String sb = timeFormatter.format(forecast.dateTime()) + ": " +
                forecast.temperature() + "°C" +
                " (по ощущению " + forecast.feelsLikeTemperature() + "°C)";
        return sb;
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

    /**
     * Проверяет, содержит ли список прогнозов погоды прогнозы с разными местами
     *
     * @param forecasts список прогнозов погоды
     * @return true, если в списке прогнозов погоды есть элементы с отличающимися местами, или false, если все прогнозы
     * погоды относятся к одному месту
     */
    private boolean hasDifferentPlaces(List<WeatherForecast> forecasts) {
        if (forecasts.isEmpty()) {
            return false;
        }
        return forecasts.stream().anyMatch(forecast -> forecast.place() != forecasts.get(0).place());
    }
}
