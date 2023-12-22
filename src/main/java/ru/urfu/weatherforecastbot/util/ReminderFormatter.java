package ru.urfu.weatherforecastbot.util;

import ru.urfu.weatherforecastbot.model.Reminder;

import java.util.List;

/**
 * Форматировщик напоминаний
 */
public interface ReminderFormatter {

    /**
     * Форматирует напоминания в строковый вид
     *
     * @param reminders список прогнозов погоды на сегодня
     * @return напоминания в строковом виде
     */
    String formatReminders(List<Reminder> reminders);

}
