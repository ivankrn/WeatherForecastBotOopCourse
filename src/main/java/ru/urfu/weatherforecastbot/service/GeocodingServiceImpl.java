package ru.urfu.weatherforecastbot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.urfu.weatherforecastbot.model.Place;

@Service
public class GeocodingServiceImpl implements GeocodingService {
    
    private final String BASE_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(GeocodingServiceImpl.class);

    private final WebClient webClient = WebClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    @Override
    public Place findPlaceByName(String name) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("name", name)
                        .queryParam("count", 1)
                        .queryParam("language", "ru")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> node.path("results").path(0))
                .map(node -> {
                    try {
                        return mapper.treeToValue(node, Place.class);
                    } catch (JsonProcessingException e) {
                        logger.error(e.getMessage());
                        return null;
                    }
                })
                .block();
    }
}
