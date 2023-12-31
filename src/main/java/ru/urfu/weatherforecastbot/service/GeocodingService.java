package ru.urfu.weatherforecastbot.service;

import ru.urfu.weatherforecastbot.model.Place;

import java.util.Optional;

/**
 * Сервис для получения широты, долготы и часового пояса по названию места
 */
public interface GeocodingService {

    /**
     * Находит место (и его координаты с часовым поясом) по названию
     *
     * @param name название места
     * @return место с координатами и часовым поясом, или пустой Optional, если место не найдено
     */
    Optional<Place> findPlaceByName(String name);

}
