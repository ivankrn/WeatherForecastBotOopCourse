package ru.urfu.weatherforecastbot.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Обработчик сообщений
 */
public interface MessageHandler {

    /**
     * Обрабатывает сообщение и возвращает ответное сообщение
     *
     * @param message сообщение
     * @return ответное сообщение
     */
    SendMessage handle(Message message);

}
