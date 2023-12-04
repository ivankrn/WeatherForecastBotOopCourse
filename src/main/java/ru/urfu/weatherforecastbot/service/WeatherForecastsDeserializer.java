package ru.urfu.weatherforecastbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import ru.urfu.weatherforecastbot.model.Place;
import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Десериализатор ответа сервера в список прогнозов погоды
 */
public class WeatherForecastsDeserializer {

    /**
     * Форматировщик даты и времени для парсинга
     */
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    /**
     * Сообщение исключения
     */
    private final static String EXCEPTION_MESSAGE = "Wrong json provided: ";

    /**
     * Преобразует ответ сервера в список прогнозов погоды
     *
     * @param response ответ сервера
     * @return список прогнозов погоды
     */
    public List<WeatherForecast> parseJsonResponseToWeatherForecasts(Place place, JsonNode response)
            throws IllegalArgumentException {
        JsonNode hourlyData = response.get("hourly");
        if (hourlyData == null) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE + response);
        }
        JsonNode times = hourlyData.get("time");
        JsonNode temperatures = hourlyData.get("temperature_2m");
        JsonNode feelsLikeTemperatures = hourlyData.get("apparent_temperature");
        if (times == null || temperatures == null || feelsLikeTemperatures == null
                || times.size() != temperatures.size()
                || times.size() != feelsLikeTemperatures.size()) {
            throw new IllegalArgumentException(EXCEPTION_MESSAGE + response);
        }
        List<WeatherForecast> forecasts = new ArrayList<>(times.size());
        for (int i = 0; i < times.size(); i++) {
            forecasts.add(new WeatherForecast(
                    place,
                    LocalDateTime.parse(times.get(i).asText(), dateTimeFormatter),
                    temperatures.get(i).asDouble(),
                    feelsLikeTemperatures.get(i).asDouble()
            ));
        }
        return forecasts;
    }

}
