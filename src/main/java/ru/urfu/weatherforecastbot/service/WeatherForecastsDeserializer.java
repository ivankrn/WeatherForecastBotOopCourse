package ru.urfu.weatherforecastbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.util.PressureConverter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// TODO: 03.11.2023 Спросить, нужно ли делать интерфейс
/**
 * Десериализатор ответа сервера в список прогнозов погоды
 */
@Component
public class WeatherForecastsDeserializer {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /**
     * Преобразует ответ сервера в список прогнозов погоды
     *
     * @param response ответ сервера
     * @return список прогнозов погоды
     */
    public List<WeatherForecast> parseJsonResponseToWeatherForecasts(JsonNode response) {
        JsonNode hourlyData = response.get("hourly");
        JsonNode times = hourlyData.get("time");
        JsonNode temperatures = hourlyData.get("temperature_2m");
        JsonNode humidities = hourlyData.get("relativehumidity_2m");
        JsonNode feelsLikeTemperatures = hourlyData.get("apparent_temperature");
        JsonNode pressures = hourlyData.get("surface_pressure");
        List<WeatherForecast> forecasts = new ArrayList<>(times.size());
        for (int i = 0; i < times.size(); i++) {
            forecasts.add(new WeatherForecast(
                    LocalDateTime.parse(times.get(i).asText(), dateTimeFormatter),
                    temperatures.get(i).asDouble(),
                    feelsLikeTemperatures.get(i).asDouble(),
                    PressureConverter.convertHpaToMmhg(pressures.get(i).asDouble()),
                    humidities.get(i).asDouble())
            );
        }
        return forecasts;
    }

}
