package ru.urfu.weatherforecastbot.model;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.Optional;

/**
 * Состояние чата
 */
@Entity
@Table(name = "chat_state")
public class ChatState {

    /**
     * ID чата
     */
    @Id
    @Column(name = "chat_id", nullable = false)
    private long chatId;

    /**
     * Место прогноза погоды (если пользователь в состоянии запроса погоды или создания напоминания)
     */
    @Column(name = "place_name")
    private String placeName;

    /**
     * Время прогноза погоды (если пользователь в состоянии запроса погоды или создания напоминания)
     */
    @Column(name = "time")
    private String time;

    /**
     * Состояние бота
     */
    @Enumerated
    private BotState botState;

    /**
     * Возвращает ID чата
     *
     * @return ID чата
     */
    public long getChatId() {
        return chatId;
    }

    /**
     * Устанавливает ID чата
     *
     * @param chatId ID чата
     */
    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    /**
     * Возвращает название места
     *
     * @return название места
     */
    public String getPlaceName() {
        return placeName;
    }

    /**
     * Устанавливает название места
     *
     * @param placeName название места
     */
    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    /**
     * Возвращает время
     *
     * @return время
     */
    public Optional<String> getTime() {
        return Optional.ofNullable(time);
    }

    /**
     * Устанавливает время
     *
     * @param time время
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Возвращает состояние бота
     *
     * @return состояние бота
     */
    public BotState getBotState() {
        return botState;
    }

    /**
     * Устанавливает состояние бота
     *
     * @param botState состояние бота
     */
    public void setBotState(BotState botState) {
        this.botState = botState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatState chatState = (ChatState) o;
        return chatId == chatState.chatId && Objects.equals(placeName, chatState.placeName) && botState == chatState.botState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, placeName, botState);
    }
}
