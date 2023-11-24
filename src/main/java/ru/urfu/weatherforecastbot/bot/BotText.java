package ru.urfu.weatherforecastbot.bot;

/**
 * Шаблоны текстов ответа бота
 */
public enum BotText {

    /**
     * Неправильная команда
     */
    WRONG_COMMAND_SYNTAX("Команда введена неверно, пожалуйста, используйте команду /help, чтобы прочитать инструкцию."),
    /**
     * Неизвестная команда
     */
    UNKNOWN_COMMAND("Извините, я не знаю такой команды."),
    /**
     * Ненайденное место
     */
    NOT_FOUND("Извините, данное место не найдено."),
    /**
     * Меню помощи
     */
    HELP_COMMAND("""
                 Вы зашли в меню помощи. Для вас доступны следующие команды:
                 /start - запустить бота
                 /help - меню помощи
                 /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                 /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
                 """),
    /**
     * Меню старта
     */
    START_COMMAND("""
                  Здравствуйте! Я бот для просмотра прогноза погоды. Доступны следующие команды:
                  /start - запустить бота
                  /help - меню помощи
                  /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                  /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
                  """);

    /**
     * Текст ответа
     */
    private final String text;

    BotText(String text) {
        this.text = text;
    }

    /**
     * Возвращает текст ответа
     *
     * @return текст ответа
     */
    public String getText() {
        return text;
    }
}
