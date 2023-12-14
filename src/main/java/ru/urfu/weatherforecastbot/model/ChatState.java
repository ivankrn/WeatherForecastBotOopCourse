package ru.urfu.weatherforecastbot.model;

import jakarta.persistence.*;
import ru.urfu.weatherforecastbot.bot.state.BotState;

import java.util.Objects;

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
        ChatState chatContext = (ChatState) o;
        return chatId == chatContext.chatId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId);
    }
}
