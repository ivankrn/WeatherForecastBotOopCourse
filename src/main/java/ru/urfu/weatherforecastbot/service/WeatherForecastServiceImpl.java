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

@Service
public class WeatherForecastServiceImpl implements WeatherForecastService {

    private final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private final GeocodingService geocodingService;
    private final WeatherForecastsDeserializer weatherForecastsDeserializer;
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
        Place place = geocodingService.findPlaceByName(placeName);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", place.latitude())
                        .queryParam("longitude", place.longitude())
                        .queryParam("hourly", "temperature_2m,relativehumidity_2m,apparent_temperature,surface_pressure")
                        .queryParam("timezone", place.timezone())
                        .queryParam("forecast_days", daysCount)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(weatherForecastsDeserializer::parseJsonResponseToWeatherForecasts)
                .block();
    }

}
