package ru.urfu.weatherforecastbot.bot;

/**
 * Шаблоны текстов ответа бота
 */
public class BotConstants {

    /**
     * Команда старта
     */
    public static final String COMMAND_START = "/start";
    /**
     * Команда помощи
     */
    public static final String COMMAND_HELP = "/help";
    /**
     * Команда прогноза на сегодня
     */
    public static final String COMMAND_FORECAST_TODAY = "/info";
    /**
     * Команда прогноза на неделю
     */
    public static final String COMMAND_FORECAST_WEEK = "/info_week";
    /**
     * Callback кнопки "Узнать прогноз"
     */
    public static final String CALLBACK_FORECAST = "/forecast";
    /**
     * Команда отмены
     */
    public static final String COMMAND_CANCEL = "/cancel";
    /**
     * Неизвестная команда
     */
    public static final String UNKNOWN_COMMAND = "Извините, я не знаю такой команды.";
    /**
     * Ненайденное место
     */
    public static final String NOT_FOUND_PLACE = "Извините, данное место не найдено.";
    /**
     * Меню помощи
     */
    public static final String HELP_TEXT = """
            Вы зашли в меню помощи. Для вас доступны следующие команды:
            /start - запустить бота
            /help - меню помощи
            /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
            /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
            """;
    /**
     * Меню старта
     */
    public static final String START_TEXT = """
            Здравствуйте! Я бот для просмотра прогноза погоды. Доступны следующие команды:
            /start - запустить бота
            /help - меню помощи
            /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
            /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
            """;
    /**
     * Текст кнопки "узнать прогноз"
     */
    public static final String FORECAST_BUTTON_TEXT = "Узнать прогноз";
    /**
     * Текст кнопки "отмена"
     */
    public static final String CANCEL_BUTTON_TEXT = "Отмена";
    /**
     * Текст кнопки "помощь"
     */
    public static final String HELP_BUTTON_TEXT = "Помощь";
}
