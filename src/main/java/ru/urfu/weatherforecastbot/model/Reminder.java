package ru.urfu.weatherforecastbot.model;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Напоминание.
 * <p>Содержит информацию о том, в какой чат, когда и по какому месту необходимо отправлять прогноз погоды</p>
 */
@Entity
public class Reminder {
    /**
     * ID напоминания
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    /**
     * ID чата, в который отправляется напоминание
     */
    @Column(name = "chat_id", nullable = false)
    private long chatId;
    /**
     * Место прогноза погоды для напоминания
     */
    @Column(name = "place_name", nullable = false)
    private String placeName;
    /**
     * Время отправки напоминания (в UTC)
     */
    @Column(name = "time", nullable = false)
    private LocalTime time;

    /**
     * Возвращает ID напоминания
     *
     * @return ID напоминания
     */
    public Long getId() {
        return id;
    }


    /**
     * Устанавливает ID напоминания
     *
     * @param id ID напоминания
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Возвращает ID чата, в который отправляется напоминание
     *
     * @return ID чата, в который отправляется напоминание
     */
    public long getChatId() {
        return chatId;
    }


    /**
     * Устанавливает ID чата, в который отправляется напоминание
     *
     * @param chatId ID чата, в который отправляется напоминание
     */
    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    /**
     * Возвращает место прогноза погоды для напоминания
     *
     * @return место прогноза погоды для напоминания
     */
    public String getPlaceName() {
        return placeName;
    }

    /**
     * Устанавливает место прогноза погоды для напоминания
     *
     * @param placeName место прогноза погоды для напоминания
     */
    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    /**
     * Возвращает время отправки напоминания (в UTC)
     *
     * @return время отправки напоминания (в UTC)
     */
    public LocalTime getTime() {
        return time;
    }

    /**
     * Устанавливает время отправки напоминания (в UTC)
     *
     * @param time время отправки напоминания (в UTC)
     */
    public void setTime(LocalTime time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(id, reminder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
