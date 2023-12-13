package ru.urfu.weatherforecastbot.bot.state.handler;

import ru.urfu.weatherforecastbot.bot.BotMessage;

/**
 * Обработчик состояния
 */
public interface StateHandler {

    /**
     * Обрабатывает состояние чата и возвращает ответное сообщение
     *
     * @param chatId ID чата
     * @param text   текст сообщения пользователя
     * @return ответное сообщение
     */
    BotMessage handle(long chatId, String text);

}
