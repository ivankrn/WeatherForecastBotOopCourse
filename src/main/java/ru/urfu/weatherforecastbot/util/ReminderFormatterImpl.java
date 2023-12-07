package ru.urfu.weatherforecastbot.util;

import org.springframework.stereotype.Component;
import ru.urfu.weatherforecastbot.model.Reminder;

import java.util.List;

/**
 * Форматирование напоминаний для удобного чтения
 */
@Component
public class ReminderFormatterImpl {

    /**
     * Форматирует напоминания в строковый вид
     *
     * @param reminders список прогнозов погоды на сегодня
     * @return напоминания в строковом виде
     */
    public String formatReminders(List<Reminder> reminders) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < reminders.size(); i++) {
            Reminder reminder = reminders.get(i);
            sb.append(i + 1).append(") ");
            sb.append(reminder.getPlaceName()).append(", ");
            sb.append(reminder.getTime()).append("\n");
        }
        return sb.toString();
    }
}
