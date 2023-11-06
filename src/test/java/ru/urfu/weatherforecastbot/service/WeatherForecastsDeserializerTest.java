package ru.urfu.weatherforecastbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты десериализатора прогнозов погоды
 */
class WeatherForecastsDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private WeatherForecastsDeserializer deserializer;

    @BeforeEach
    void setUp() {
        deserializer = new WeatherForecastsDeserializer();
    }

    @Test
    @DisplayName("При валидном json десериализация должна проходить успешно")
    void givenValidJson_whenDeserialize_thenReturnForecasts() throws JsonProcessingException {
        String json = "{\n" +
                "  \"latitude\": 56.875,\n" +
                "  \"longitude\": 60.625,\n" +
                "  \"generationtime_ms\": 0.0680685043334961,\n" +
                "  \"utc_offset_seconds\": 18000,\n" +
                "  \"timezone\": \"Asia/Yekaterinburg\",\n" +
                "  \"timezone_abbreviation\": \"+05\",\n" +
                "  \"elevation\": 254,\n" +
                "  \"hourly_units\": {\n" +
                "    \"time\": \"iso8601\",\n" +
                "    \"temperature_2m\": \"°C\",\n" +
                "    \"relativehumidity_2m\": \"%\",\n" +
                "    \"apparent_temperature\": \"°C\",\n" +
                "    \"surface_pressure\": \"hPa\"\n" +
                "  },\n" +
                "  \"hourly\": {\n" +
                "    \"time\": [\n" +
                "      \"2023-11-05T00:00\",\n" +
                "      \"2023-11-05T01:00\",\n" +
                "      \"2023-11-05T02:00\"\n" +
                "    ],\n" +
                "    \"temperature_2m\": [\n" +
                "      -3.6,\n" +
                "      -3.8,\n" +
                "      -3.8\n" +
                "    ],\n" +
                "    \"relativehumidity_2m\": [\n" +
                "      82,\n" +
                "      82,\n" +
                "      82\n" +
                "    ],\n" +
                "    \"apparent_temperature\": [\n" +
                "      -7.5,\n" +
                "      -7.7,\n" +
                "      -7.7\n" +
                "    ],\n" +
                "    \"surface_pressure\": [\n" +
                "      995.1,\n" +
                "      995.8,\n" +
                "      995.8\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        JsonNode jsonNode = mapper.readTree(json);
        List<WeatherForecast> expected = List.of(
                new WeatherForecast(
                        LocalDateTime.of(2023, 11, 5, 0, 0),
                        -3.6,
                        -7.5,
                        746,
                        82
                ),
                new WeatherForecast(
                        LocalDateTime.of(2023, 11, 5, 1, 0),
                        -3.8,
                        -7.7,
                        746,
                        82
                ),
                new WeatherForecast(
                        LocalDateTime.of(2023, 11, 5, 2, 0),
                        -3.8,
                        -7.7,
                        746,
                        82
                )
        );
        assertEquals(expected, deserializer.parseJsonResponseToWeatherForecasts(jsonNode));
    }

    @Test
    @DisplayName("При отсутствии необходимых полей должно быть выброшено исключение")
    void givenMalformedJson_whenDeserialize_thenExceptionThrown() throws JsonProcessingException {
        String json = "{\"field\": \"value\"}";
        JsonNode jsonNode = mapper.readTree(json);
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.parseJsonResponseToWeatherForecasts(jsonNode));
    }

    @Test
    @DisplayName("При недостаточном количестве значений данных должно быть выброшено исключение")
    void givenNotEnoughData_whenDeserialize_thenExceptionThrown() throws JsonProcessingException {
        String json = "{\n" +
                "  \"latitude\": 56.875,\n" +
                "  \"longitude\": 60.625,\n" +
                "  \"generationtime_ms\": 0.0680685043334961,\n" +
                "  \"utc_offset_seconds\": 18000,\n" +
                "  \"timezone\": \"Asia/Yekaterinburg\",\n" +
                "  \"timezone_abbreviation\": \"+05\",\n" +
                "  \"elevation\": 254,\n" +
                "  \"hourly_units\": {\n" +
                "    \"time\": \"iso8601\",\n" +
                "    \"temperature_2m\": \"°C\",\n" +
                "    \"relativehumidity_2m\": \"%\",\n" +
                "    \"apparent_temperature\": \"°C\",\n" +
                "    \"surface_pressure\": \"hPa\"\n" +
                "  },\n" +
                "  \"hourly\": {\n" +
                "    \"time\": [\n" +
                "      \"2023-11-05T00:00\",\n" +
                "      \"2023-11-05T01:00\",\n" +
                "      \"2023-11-05T02:00\"\n" +
                "    ],\n" +
                "    \"temperature_2m\": [\n" +
                "      -3.8\n" +
                "    ],\n" +
                "    \"relativehumidity_2m\": [\n" +
                "      82,\n" +
                "      82,\n" +
                "      82\n" +
                "    ],\n" +
                "    \"apparent_temperature\": [\n" +
                "      -7.5,\n" +
                "      -7.7,\n" +
                "      -7.7\n" +
                "    ],\n" +
                "    \"surface_pressure\": [\n" +
                "      995.1,\n" +
                "      995.8,\n" +
                "      995.8\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        JsonNode jsonNode = mapper.readTree(json);
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.parseJsonResponseToWeatherForecasts(jsonNode));
    }
}