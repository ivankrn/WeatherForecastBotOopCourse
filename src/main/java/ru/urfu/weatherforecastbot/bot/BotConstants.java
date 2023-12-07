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
     * Команда отмены
     */
    public static final String COMMAND_CANCEL = "/cancel";
    /**
     * Команда создания напоминания прогноза
     */
    public static final String COMMAND_SUBSCRIBE = "/subscribe";
    /**
     * Команда просмотра списка прогнозов
     */
    public static final String COMMAND_SHOW_SUBSCRIPTIONS = "/show_subscriptions";
    /**
     * Команда для напоминания прогноза
     */
    public static final String COMMAND_DEL_SUBSCRIPTION = "/del_subscription";
    /**
     * Команда редактирования напоминания прогноза
     */
    public static final String COMMAND_EDIT_SUBSCRIPTION = "/edit_subscription";
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
            /subscribe <название населенного пункта> <время по Гринвичу> - создать напоминание прогноза погоды
            /show_subscriptions - показать список напоминаний
            /edit_subscription <номер напоминания> <новое название населенного пункта> <новое время по Гринвичу> - изменить напоминание прогноза погоды
            /del_subscription <номер напоминания> - удалить напоминание с указанным номером
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
            /subscribe <название населенного пункта> <время по Гринвичу> - создать напоминание прогноза погоды
            /show_subscriptions - показать список напоминаний
            /edit_subscription <номер напоминания> <новое название населенного пункта> <новое время по Гринвичу> - изменить напоминание прогноза погоды
            /del_subscription <номер напоминания> - удалить напоминание с указанным номером
            """;

    /**
     * Понимаю только текст
     */
    public static final String UNDERSTAND_ONLY_TEXT = "Извините, я понимаю только текст.";
    /**
     * Сегодня
     */
    public static final String TODAY = "Сегодня";
    /**
     * Завтра
     */
    public static final String TOMORROW = "Завтра";
    /**
     * Неделя
     */
    public static final String WEEK = "Неделя";
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
    /**
     * Создание напоминания
     */
    public static final String ADDED_SUBSCRIPTION = "Напоминание создано. Буду присылать прогноз погоды в";
    /**
     * Изменение напоминания
     */
    public static final String EDITED_SUBSCRIPTION = "Напоминание изменено. Буду присылать прогноз погоды в";
    /**
     * Напоминание удалено
     */
    public static final String DELETED_SUBSCRIPTION = "Напоминание удалено. Больше не буду присылать прогноз погоды.";
    /**
     * Нет напоминания под таким номером
     */
    public static final String NO_REMINDER_WITH_POSITION = "Нет напоминания с таким номером.";
    /**
     * Некорректный формат номера напоминания
     */
    public static final String NOT_A_NUMBER_REMINDER_POSITION = "Некорректный формат номера напоминания. " +
            "Используйте только числа при вводе.";
    /**
     * Некорректный формат времени
     */
    public static final String WRONG_REMINDER_TIME = "Некорректный формат времени. " +
            "Введите время в виде 00:00 (часы:минуты)";
    /**
     * Неустановленные напоминания
     */
    public static final String NO_REMINDERS = "У вас не установлены напоминания о прогнозе погоды.";
}
