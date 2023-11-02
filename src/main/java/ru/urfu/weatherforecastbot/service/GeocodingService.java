package ru.urfu.weatherforecastbot.service;

import ru.urfu.weatherforecastbot.model.Place;

/**
 * Сервис для получения широты и долготы по названию места
 */
public interface GeocodingService {

    /**
     * Находит место (и его координаты с часовым поясом) по названию
     *
     * @param name название места
     * @return место с координатами и часовым поясом
     */
    Place findPlaceByName(String name);

}
