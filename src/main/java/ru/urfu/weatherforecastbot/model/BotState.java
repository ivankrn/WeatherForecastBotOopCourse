package ru.urfu.weatherforecastbot.model;

/**
 * Состояние бота
 */
public enum BotState {
    /**
     * Исходное состояние (ожидание выбора действия пользователя)
     */
    INITIAL,
    /**
     * Ожидание ввода места для прогноза погоды
     */
    WAITING_FOR_PLACE_NAME,
    /**
     * Ожидание выбора временного периода для прогноза погоды
     */
    WAITING_FOR_TIME_PERIOD
}
