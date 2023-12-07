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
    WAITING_FOR_FORECAST_PLACE_NAME,
    /**
     * Ожидание выбора временного периода для прогноза погоды
     */
    WAITING_FOR_FORECAST_TIME_PERIOD,
    /**
     * Ожидание ввода места для создания напоминания
     */
    WAITING_FOR_REMINDER_PLACE_NAME,
    /**
     * Ожидание ввода времени для создания напоминания
     */
    WAITING_FOR_REMINDER_TIME,
    /**
     * Ожидание ввода позиции напоминания, которое надо удалить
     */
    WAITING_FOR_REMINDER_POSITION_TO_DELETE
}
