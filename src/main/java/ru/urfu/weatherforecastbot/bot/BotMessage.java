package ru.urfu.weatherforecastbot.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сообщение
 */
public class BotMessage {

    /**
     * Текст сообщения
     */
    private String text;
    /**
     * Кнопки сообщения
     */
    private List<Button> buttons = new ArrayList<>();

    /**
     * Возвращает текст кнопки
     *
     * @return текст кнопки
     */
    public String getText() {
        return text;
    }

    /**
     * Устанавливает текст кнопки, если он не пустой
     *
     * @param text текст кнопки
     */
    public void setText(String text) {
        if (!text.isBlank()) {
            this.text = text;
        }
    }

    /**
     * Возвращает кнопки сообщения
     *
     * @return кнопки сообщения
     */
    public List<Button> getButtons() {
        return buttons;
    }

    /**
     * Устанавливает кнопки сообщения
     *
     * @param buttons кнопки сообщения
     */
    public void setButtons(List<Button> buttons) {
        this.buttons = buttons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotMessage that = (BotMessage) o;
        return Objects.equals(text, that.text) && Objects.equals(buttons, that.buttons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, buttons);
    }
}
