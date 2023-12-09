package ru.urfu.weatherforecastbot.util;

/**
 * Временной период
 */
public enum ForecastTimePeriod {

    /**
     * Сегодня
     */
    TODAY("Сегодня"),
    /**
     * Завтра
     */
    TOMORROW("Завтра"),
    /**
     * Неделя
     */
    WEEK("Неделя");

    private final String text;

    ForecastTimePeriod(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
