package ru.urfu.weatherforecastbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты десериализатора прогнозов погоды
 */
class WeatherForecastsDeserializerTest {

    /**
     * JSON маппер
     */
    private final ObjectMapper mapper = new ObjectMapper();
    /**
     * Десериализатор ответа сервера прогнозов погоды
     */
    private final WeatherForecastsDeserializer deserializer = new WeatherForecastsDeserializer();

    @Test
    @DisplayName("При валидном json десериализация должна проходить успешно")
    void givenValidJson_whenDeserialize_thenReturnForecasts() throws JsonProcessingException {
        String json = """
                {
                  "latitude": 56.875,
                  "longitude": 60.625,
                  "generationtime_ms": 0.0680685043334961,
                  "utc_offset_seconds": 18000,
                  "timezone": "Asia/Yekaterinburg",
                  "timezone_abbreviation": "+05",
                  "elevation": 254,
                  "hourly_units": {
                    "time": "iso8601",
                    "temperature_2m": "°C",
                    "relativehumidity_2m": "%",
                    "apparent_temperature": "°C",
                    "surface_pressure": "hPa"
                  },
                  "hourly": {
                    "time": [
                      "2023-11-05T00:00",
                      "2023-11-05T01:00",
                      "2023-11-05T02:00"
                    ],
                    "temperature_2m": [
                      -3.6,
                      -3.8,
                      -3.8
                    ],
                    "relativehumidity_2m": [
                      82,
                      82,
                      82
                    ],
                    "apparent_temperature": [
                      -7.5,
                      -7.7,
                      -7.7
                    ],
                    "surface_pressure": [
                      995.1,
                      995.8,
                      995.8
                    ]
                  }
                }""";
        JsonNode jsonNode = mapper.readTree(json);
        List<WeatherForecast> expected = List.of(
                new WeatherForecast(
                        LocalDateTime.of(2023, 11, 5, 0, 0),
                        -3.6,
                        -7.5
                ),
                new WeatherForecast(
                        LocalDateTime.of(2023, 11, 5, 1, 0),
                        -3.8,
                        -7.7
                ),
                new WeatherForecast(
                        LocalDateTime.of(2023, 11, 5, 2, 0),
                        -3.8,
                        -7.7
                )
        );
        assertEquals(expected, deserializer.parseJsonResponseToWeatherForecasts(jsonNode));
    }

    @Test
    @DisplayName("При отсутствии необходимых полей должно быть выброшено исключение")
    void givenMalformedJson_whenDeserialize_thenExceptionThrown() throws JsonProcessingException {
        String json = "{\"field\":\"value\"}";
        JsonNode jsonNode = mapper.readTree(json);
        String actualExceptionMessage = assertThrows(IllegalArgumentException.class,
                () -> deserializer.parseJsonResponseToWeatherForecasts(jsonNode)).getMessage();
        assertEquals("Wrong json provided: " + jsonNode, actualExceptionMessage);
    }

    @Test
    @DisplayName("При недостаточном количестве значений данных должно быть выброшено исключение")
    void givenNotEnoughData_whenDeserialize_thenExceptionThrown() throws JsonProcessingException {
        String json = """
                {
                  "latitude": 56.875,
                  "longitude": 60.625,
                  "generationtime_ms": 0.0680685043334961,
                  "utc_offset_seconds": 18000,
                  "timezone": "Asia/Yekaterinburg",
                  "timezone_abbreviation": "+05",
                  "elevation": 254,
                  "hourly_units": {
                    "time": "iso8601",
                    "temperature_2m": "°C",
                    "relativehumidity_2m": "%",
                    "apparent_temperature": "°C",
                    "surface_pressure": "hPa"
                  },
                  "hourly": {
                    "time": [
                      "2023-11-05T00:00",
                      "2023-11-05T01:00",
                      "2023-11-05T02:00"
                    ],
                    "temperature_2m": [
                      -3.8
                    ],
                    "relativehumidity_2m": [
                      82,
                      82,
                      82
                    ],
                    "apparent_temperature": [
                      -7.5,
                      -7.7,
                      -7.7
                    ],
                    "surface_pressure": [
                      995.1,
                      995.8,
                      995.8
                    ]
                  }
                }""";
        JsonNode jsonNode = mapper.readTree(json);
        String actualExceptionMessage = assertThrows(IllegalArgumentException.class,
                () -> deserializer.parseJsonResponseToWeatherForecasts(jsonNode)).getMessage();
        assertEquals("Wrong json provided: " + jsonNode, actualExceptionMessage);
    }
}