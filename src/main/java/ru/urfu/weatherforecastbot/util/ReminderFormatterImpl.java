package ru.urfu.weatherforecastbot.util;

import ru.urfu.weatherforecastbot.model.Reminder;

import java.util.List;

public class ReminderFormatterImpl implements ReminderFormatter {

    @Override
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
