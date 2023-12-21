package ru.urfu.weatherforecastbot.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.weatherforecastbot.model.Reminder;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тесты форматировщика напоминаний
 */
class ReminderFormatterImplTest {

    /**
     * Форматировщик напоминаний
     */
    private final ReminderFormatterImpl formatter = new ReminderFormatterImpl();

    /**
     * Проверяет корректное форматирование списка напоминаний в виде строки при наличии напоминаний.<br>
     * Проверки:
     * <ul>
     *     <li>Если список напоминаний не пуст,
     *     то метод форматирования должен вернуть отформатированный список напоминаний.</li>
     * </ul>
     */
    @Test
    @DisplayName("При непустом списке напоминаний должен возвращать список напоминаний в виде строки")
    void givenNotEmptyReminders_whenFormatReminders_thenReturnFormattedReminders() {
        long chatId = 1L;
        String placeName = "Екатеринбург";
        Reminder firstReminder = new Reminder();
        firstReminder.setId(1L);
        firstReminder.setChatId(chatId);
        firstReminder.setPlaceName(placeName);
        firstReminder.setTime(LocalTime.of(5, 0));
        Reminder secondReminder = new Reminder();
        secondReminder.setId(2L);
        secondReminder.setChatId(chatId);
        secondReminder.setPlaceName(placeName);
        secondReminder.setTime(LocalTime.of(17, 0));
        List<Reminder> reminders = List.of(firstReminder, secondReminder);
        String expected = """
                1) Екатеринбург, 05:00
                2) Екатеринбург, 17:00
                """;

        String actual = formatter.formatReminders(reminders);

        assertEquals(expected, actual);
    }

    /**
     * Проверяет корректное возвращение пустой строки при отсутствии напоминаний.<br>
     * <br>
     * Проверки:
     * <ul>
     *     <li>Если список напоминаний пуст, то метод форматирования должен вернуть пустую строку.</li>
     * </ul>
     */
    @Test
    @DisplayName("При пустом списке напоминаний должен возвращать только пустую строку")
    void givenEmptyReminders_whenFormatReminders_thenReturnEmptyString() {
        List<Reminder> reminders = List.of();

        String actual = formatter.formatReminders(reminders);

        assertEquals("", actual);
    }
}