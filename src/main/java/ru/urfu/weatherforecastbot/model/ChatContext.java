package ru.urfu.weatherforecastbot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/**
 * Контекст чата
 */
@Entity
@Table(name = "chat_context")
public class ChatContext {

    /**
     * ID чата
     */
    @Id
    @Column(name = "chat_id", nullable = false)
    private long chatId;

    /**
     * Место прогноза погоды (если пользователь в состоянии запроса погоды или добавления / редактирования напоминания)
     */
    @Column(name = "place_name")
    private String placeName;

    /**
     * Относительная позиция напоминания для редактирования (если пользователь в состоянии редактирования напоминания)
     */
    @Column(name = "reminder_position")
    private Integer reminderPosition;

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
     * Возвращает относительную позицию напоминания
     *
     * @return позиция напоминания
     */
    public Integer getReminderPosition() {
        return reminderPosition;
    }

    /**
     * Устанавливает относительную позицию напоминания
     *
     * @param reminderPosition позиция напоминания
     */
    public void setReminderPosition(Integer reminderPosition) {
        this.reminderPosition = reminderPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatContext chatContext = (ChatContext) o;
        return chatId == chatContext.chatId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }
}
