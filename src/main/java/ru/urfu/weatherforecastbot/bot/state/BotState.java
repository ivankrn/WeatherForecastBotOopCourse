package ru.urfu.weatherforecastbot.bot.state;

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
    WAITING_FOR_TIME_PERIOD,
    /**
     * Ожидание ввода места для прогноза погоды на сегодня
     */
    WAITING_FOR_TODAY_FORECAST_PLACE_NAME,
    /**
     * Ожидание ввода места для прогноза погоды на неделю
     */
    WAITING_FOR_WEEK_FORECAST_PLACE_NAME
}
