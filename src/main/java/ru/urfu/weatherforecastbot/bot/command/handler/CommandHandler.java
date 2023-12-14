package ru.urfu.weatherforecastbot.bot.command.handler;

import ru.urfu.weatherforecastbot.bot.BotMessage;

/**
 * Обработчик команды
 */
public interface CommandHandler {

    /**
     * Обрабатывает команду чата с указанным ID и возвращает ответное сообщение
     *
     * @param chatId      ID чата
     * @param userMessage текст сообщения пользователя
     * @return ответное сообщение
     */
    BotMessage handle(long chatId, String userMessage);

}
