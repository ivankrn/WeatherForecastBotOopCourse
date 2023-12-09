package ru.urfu.weatherforecastbot.bot.state;

/**
 * Переход из одного состояния в другое
 *
 * @param from начальное состояние
 * @param to   конечное состояние
 */
public record Transition(BotState from, BotState to) {
}
