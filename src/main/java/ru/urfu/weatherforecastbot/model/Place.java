package ru.urfu.weatherforecastbot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Место
 *
 * @param name      название
 * @param latitude  широта
 * @param longitude долгота
 * @param timezone  часовой пояс
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Place(String name, double latitude, double longitude, String timezone) {
}
