package ru.urfu.weatherforecastbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.util.PressureConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO: 03.11.2023 Спросить, нужно ли делать интерфейс
/**
 * Десериализатор ответа сервера в список прогнозов погоды
 */
@Component
public class WeatherForecastsDeserializer {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private final String exceptionMessage = "Wrong json provided: ";

    /**
     * Преобразует ответ сервера в список прогнозов погоды
     *
     * @param response ответ сервера
     * @return список прогнозов погоды
     */
    public List<WeatherForecast> parseJsonResponseToWeatherForecasts(JsonNode response) throws IllegalArgumentException {
        JsonNode hourlyData = response.get("hourly");
        if (hourlyData == null) {
            throw new IllegalArgumentException(exceptionMessage + response.asText());
        }
        JsonNode times = hourlyData.get("time");
        JsonNode temperatures = hourlyData.get("temperature_2m");
        JsonNode humidities = hourlyData.get("relativehumidity_2m");
        JsonNode feelsLikeTemperatures = hourlyData.get("apparent_temperature");
        JsonNode pressures = hourlyData.get("surface_pressure");
        if (times == null || temperatures == null || humidities == null
                || feelsLikeTemperatures == null || pressures == null
                || times.size() != temperatures.size()
                || times.size() != humidities.size()
                || times.size() != feelsLikeTemperatures.size()
                || times.size() != pressures.size()) {
            throw new IllegalArgumentException(exceptionMessage + response.asText());
        }
        List<WeatherForecast> forecasts = new ArrayList<>(times.size());
        for (int i = 0; i < times.size(); i++) {
            forecasts.add(new WeatherForecast(
                    LocalDateTime.parse(times.get(i).asText(), dateTimeFormatter),
                    temperatures.get(i).asDouble(),
                    feelsLikeTemperatures.get(i).asDouble(),
                    (int) PressureConverter.convertHpaToMmhg(pressures.get(i).asDouble()),
                    humidities.get(i).asInt())
            );
        }
        return forecasts;
    }

}
