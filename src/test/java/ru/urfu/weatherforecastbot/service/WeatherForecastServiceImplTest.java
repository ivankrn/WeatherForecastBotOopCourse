package ru.urfu.weatherforecastbot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.urfu.weatherforecastbot.model.Place;
import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Тесты сервиса для получения прогноза погоды
 */
@ExtendWith(MockitoExtension.class)
class WeatherForecastServiceImplTest {

    private final GeocodingService geocodingService;
    private final WeatherForecastService weatherForecastService;

    public WeatherForecastServiceImplTest(@Mock GeocodingService geocodingService) {
        String forecast = """
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
        WebClient fakeWebClient = WebClient.builder()
                .exchangeFunction(clientRequest -> Mono.just(ClientResponse.create(HttpStatus.OK)
                        .header("content-type", "application/json")
                        .body(forecast)
                        .build()))
                .build();
        this.geocodingService = geocodingService;
        weatherForecastService = new WeatherForecastServiceImpl(geocodingService, fakeWebClient);
    }

    @Test
    @DisplayName("Если место не найдено, должен вернуться пустой список")
    void givenNotFoundPlace_whenGetForecast_thenReturnEmptyList() {
        when(geocodingService.findPlaceByName("beautiful faraway")).thenReturn(Optional.empty());

        assertEquals(List.of(), weatherForecastService.getForecast("beautiful faraway", 1));
    }

    @Test
    @DisplayName("Если место найдено, должен вернуться не пустой список")
    void givenPlace_whenGetForecast_thenReturnNotEmptyList() {
        Place ekaterinburg = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        when(geocodingService.findPlaceByName("Екатеринбург")).thenReturn(Optional.of(ekaterinburg));

        List<WeatherForecast> forecasts = weatherForecastService.getForecast("Екатеринбург", 1);
        assertFalse(forecasts.isEmpty());
    }
}