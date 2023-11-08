package ru.urfu.weatherforecastbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.urfu.weatherforecastbot.model.Place;
import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.util.List;
import java.util.Optional;

@Service
public class WeatherForecastServiceImpl implements WeatherForecastService {

    /**
     * URL API для получения прогнозов погоды
     */
    private final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    /**
     * Сервис для поиска мест
     */
    private final GeocodingService geocodingService;
    /**
     * Десериализатор ответа сервера прогнозов погоды
     */
    private final WeatherForecastsDeserializer weatherForecastsDeserializer;
    /**
     * Клиент для запросов API
     */
    private final WebClient webClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    @Autowired
    public WeatherForecastServiceImpl(GeocodingService geocodingService, WeatherForecastsDeserializer weatherForecastsDeserializer) {
        this.geocodingService = geocodingService;
        this.weatherForecastsDeserializer = weatherForecastsDeserializer;
    }

    @Override
    public List<WeatherForecast> getForecast(String placeName, int daysCount) {
        Optional<Place> place = geocodingService.findPlaceByName(placeName);
        if (place.isEmpty()) {
            return null;
        }
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", place.get().latitude())
                        .queryParam("longitude", place.get().longitude())
                        .queryParam("hourly", "temperature_2m,apparent_temperature")
                        .queryParam("timezone", place.get().timezone())
                        .queryParam("forecast_days", daysCount)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(weatherForecastsDeserializer::parseJsonResponseToWeatherForecasts)
                .block();
    }

}
