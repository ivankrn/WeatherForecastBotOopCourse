package ru.urfu.weatherforecastbot.bot;

import java.util.Objects;

/**
 * Кнопка
 */
public class Button {

    /**
     * Текст кнопки
     */
    private String text;

    /**
     * Команда, исполняемая при нажатии
     */
    private String callback;

    /**
     * Возвращает текст кнопки
     *
     * @return текст кнопки
     */
    public String getText() {
        return text;
    }

    /**
     * Устанавливает текст кнопки, если текст не пуст
     *
     * @param text текст кнопки
     */
    public void setText(String text) {
        if (!text.isBlank()) {
            this.text = text;
        }
    }

    /**
     * Возвращает команду кнопки
     *
     * @return команда кнопки
     */
    public String getCallback() {
        return callback;
    }

    /**
     * Устанавливает команду кнопки, если команда не пуста
     *
     * @param callback команда кнопки
     */
    public void setCallback(String callback) {
        if (!callback.isBlank()) {
            this.callback = callback;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Button button = (Button) o;
        return Objects.equals(text, button.text) && Objects.equals(callback, button.callback);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, callback);
    }
}
