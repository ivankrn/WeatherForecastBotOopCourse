package ru.urfu.weatherforecastbot.bot;

/**
 * Обработчик сообщений
 */
public interface MessageHandler {

    /**
     * Обрабатывает сообщение и возвращает ответное сообщение
     *
     * @param chatId  ID чата
     * @param message сообщение
     * @return ответное сообщение
     */
    BotMessage handle(long chatId, String message);

}
