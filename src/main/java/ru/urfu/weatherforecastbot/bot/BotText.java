package ru.urfu.weatherforecastbot.bot;

/**
 * Шаблоны текстов ответа бота
 */
public enum BotText {

    /**
     * Неправильная команда
     */
    WRONG_COMMAND("Команда введена неверно, попробуйте ещё раз."),
    /**
     * Ненайденное место
     */
    NOT_FOUND("Извините, данное место не найдено.");

    public final String text;

    BotText(String text) {
        this.text = text;
    }
}
