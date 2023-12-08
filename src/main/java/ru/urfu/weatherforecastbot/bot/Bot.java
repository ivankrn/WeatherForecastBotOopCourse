package ru.urfu.weatherforecastbot.bot;

/**
 * Бот
 */
public interface Bot {

    /**
     * Отправляет сообщение в чат с указанным ID
     *
     * @param chatId ID чата
     * @param message сообщение
     */
    void sendMessage(long chatId, BotMessage message);

}
