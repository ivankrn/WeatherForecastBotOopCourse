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
     * Сообщение исключения при пустом списке прогнозов
     */
    private static final String EMPTY_FORECASTS_EXCEPTION_MESSAGE = "Forecasts are empty!";
    /**
     * Сообщение исключения при прогнозах, не относящихся к одному месту
     */
    private static final String FORECASTS_WITH_DIFFERENT_PLACES_EXCEPTION_MESSAGE = "Forecasts have different places!";
    /**
     * Интервал (в часах) форматирования на неделю
     */
    private static final int HOUR_INTERVAL = 4;
    /**
     * Значок термометра
     */
    private static final String THERMOMETER_EMOJI = "\uD83C\uDF21️";
    /**
     * Символ градусов цельсия
     */
    private static final String CELSIUS_SYMBOL = "°C";
    /**
     * Форматировщик времени для форматирования по часам
     */
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH-mm");
    /**
     * Форматировщик даты для форматирования на неделю
     */
    private final DateTimeFormatter weekDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public String formatForecasts(ForecastTimePeriod timePeriod, List<WeatherForecast> forecasts)
            throws IllegalArgumentException {
        validateForecasts(forecasts);
        String period = null;
        String formattedForecasts = null;
        switch (timePeriod) {
            case WEEK -> {
                period = "на неделю";
                formattedForecasts = formatSeveralDaysForecast(forecasts);
            }
            case TODAY -> {
                period = "на сегодня";
                formattedForecasts = formatWeatherForecasts(forecasts);
            }
            case TOMORROW -> {
                period = "на завтра";
                formattedForecasts = formatWeatherForecasts(forecasts);
            }
        }
        String header = THERMOMETER_EMOJI + " Прогноз погоды %s (%s):"
                .formatted(period, forecasts.get(0).place().name());
        return header + "\n\n" + formattedForecasts;
    }

    private String formatSeveralDaysForecast(List<WeatherForecast> forecasts) {
        Map<LocalDate, List<WeatherForecast>> forecastsByDate = forecasts.stream()
                .collect(Collectors.groupingBy(weatherForecast -> weatherForecast.dateTime().toLocalDate()));

        List<LocalDate> sortedDates = new ArrayList<>(forecastsByDate.keySet());
        Collections.sort(sortedDates);

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < sortedDates.size(); i++) {
            LocalDate date = sortedDates.get(i);
            sb.append(weekDateFormatter.format(date)).append(":\n");

            List<WeatherForecast> dateForecasts = forecastsByDate.get(date);
            String formattedForecasts = formatWeatherForecasts(dateForecasts.stream()
                    .filter(weatherForecast -> weatherForecast.dateTime().getHour() % HOUR_INTERVAL == 0)
                    .toList());

            sb.append(formattedForecasts);
            if (i < sortedDates.size() - 1) {
                sb.append("\n\n");
            }
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
        return timeFormatter.format(forecast.dateTime()) + ": " +
                forecast.temperature() + CELSIUS_SYMBOL +
                " (по ощущению " + forecast.feelsLikeTemperature() + CELSIUS_SYMBOL + ")";
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
     * Проверяет корректность списка прогнозов погоды
     *
     * @param forecasts список прогнозов погоды
     * @throws IllegalArgumentException при ошибке валидации
     */
    private void validateForecasts(List<WeatherForecast> forecasts) throws IllegalArgumentException {
        if (forecasts.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_FORECASTS_EXCEPTION_MESSAGE);
        }
        if (hasDifferentPlaces(forecasts)) {
            throw new IllegalArgumentException(FORECASTS_WITH_DIFFERENT_PLACES_EXCEPTION_MESSAGE);
        }
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
