package ru.urfu.weatherforecastbot.bot;

/**
 * Шаблоны текстов ответа бота
 */
public enum BotText {

    /**
     * Неправильная команда
     */
    WRONG_COMMAND_SYNTAX("Команда введена неверно, попробуйте ещё раз."),
    /**
     * Неизвестная команда
     */
    UNKNOWN_COMMAND("Извините, я не знаю такой команды."),
    /**
     * Ненайденное место
     */
    NOT_FOUND("Извините, данное место не найдено.");

    /**
     * Текст ответа
     */
    public final String text;

    BotText(String text) {
        this.text = text;
    }
}
