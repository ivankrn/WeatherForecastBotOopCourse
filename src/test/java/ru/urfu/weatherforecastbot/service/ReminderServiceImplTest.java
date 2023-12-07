package ru.urfu.weatherforecastbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.urfu.weatherforecastbot.bot.WeatherForecastBot;
import ru.urfu.weatherforecastbot.database.ReminderRepository;
import ru.urfu.weatherforecastbot.model.Place;
import ru.urfu.weatherforecastbot.model.Reminder;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты сервиса для управления напоминаниями
 */
@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    @Mock
    private WeatherForecastBot bot;
    /**
     * Сервис для получения прогнозов погоды
     */
    @Mock
    private WeatherForecastService weatherService;
    /**
     * Форматировщик прогноза погоды в удобочитаемый вид
     */
    @Mock
    private WeatherForecastFormatterImpl forecastFormatter;
    /**
     * Репозиторий напоминаний
     */
    private ReminderRepository reminderRepository;
    /**
     * ScheduledExecutorService для планирования выполнения задач
     */
    private FakeScheduledExecutorService executorService;
    /**
     * Сервис для управления напоминаниями
     */
    private ReminderService reminderService;

    /**
     * Подготавливает окружение перед тестами
     */
    @BeforeEach()
    void setUp() {
        reminderRepository = Mockito.mock();
        executorService = new FakeScheduledExecutorService();
        reminderService = new ReminderServiceImpl(bot, weatherService, forecastFormatter,
                reminderRepository, executorService);
    }

    /**
     * Проверяет добавление напоминания прогноза погоды.<br>
     * Проверки:
     * <ul>
     *     <li>если время напоминания ещё не прошло, то следующий раз напоминание должно придти в тот же день</li>
     *     <li>если время напоминания уже прошло, то следующий раз напоминание должно отправиться на следующий день</li>
     *     <li>если указано некорректное время, должно быть выброшено исключение</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест добавления напоминания прогноза погоды")
    void testAddReminder() {
        long chatId = 1L;
        LocalTime now = LocalTime.now(ZoneOffset.UTC);
        int deltaInMinutes = 10;
        Reminder ekateringburgReminder = new Reminder();
        ekateringburgReminder.setId(1L);
        ekateringburgReminder.setChatId(chatId);
        ekateringburgReminder.setPlaceName("Екатеринбург");
        ekateringburgReminder.setTime(now.plusMinutes(deltaInMinutes));
        Reminder nizhnyNovgorodReminder = new Reminder();
        nizhnyNovgorodReminder.setId(2L);
        nizhnyNovgorodReminder.setChatId(chatId);
        nizhnyNovgorodReminder.setPlaceName("Нижний Новгород");
        nizhnyNovgorodReminder.setTime(now.minusMinutes(deltaInMinutes));
        when(reminderRepository.save(any(Reminder.class))).thenReturn(ekateringburgReminder, nizhnyNovgorodReminder);
        LocalDateTime today = LocalDateTime.now();
        int hours = 24;
        List<WeatherForecast> ekateringburgForecast = new ArrayList<>(hours);
        List<WeatherForecast> nizhnyNovgorodForecast = new ArrayList<>(hours);
        Place ekateringburg =
                new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        Place nizhnyNovgorod =
                new Place("Нижний Новгород", 56.328, 44.002, "Europe/Moscow");
        for (int hour = 0; hour < hours; hour++) {
            ekateringburgForecast.add(
                    new WeatherForecast(ekateringburg, today.withHour(hour), 0, 0));
            nizhnyNovgorodForecast.add(
                    new WeatherForecast(nizhnyNovgorod, today.withHour(hour), 10, 5));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(ekateringburgForecast);
        when(forecastFormatter.formatTodayForecast(ekateringburgForecast))
                .thenReturn("Прогноз погоды на сегодня (Екатеринбург): ...");
        when(weatherService.getForecast("Нижний Новгород", 1))
                .thenReturn(nizhnyNovgorodForecast);
        when(forecastFormatter.formatTodayForecast(nizhnyNovgorodForecast))
                .thenReturn("Прогноз погоды на сегодня (Нижний Новгород): ...");
        String expectedEkateringburgForecast = "Напоминаю вам о погоде!\nПрогноз погоды на сегодня (Екатеринбург): ...";
        String expectedNizhnyNovgorodForecast =
                "Напоминаю вам о погоде!\nПрогноз погоды на сегодня (Нижний Новгород): ...";

        reminderService.addReminder(
                chatId,
                "Екатеринбург",
                now
                        .plusMinutes(deltaInMinutes)
                        .format(DateTimeFormatter.ISO_LOCAL_TIME));
        reminderService.addReminder(
                chatId,
                "Нижний Новгород",
                now
                        .minusMinutes(deltaInMinutes)
                        .format(DateTimeFormatter.ISO_LOCAL_TIME));

        verify(bot, never()).executeMessageWithLogging(argThat((SendMessage message) ->
                message.getText().equals(expectedEkateringburgForecast)
                        || message.getText().equals(expectedNizhnyNovgorodForecast)));

        executorService.elapse(deltaInMinutes, TimeUnit.MINUTES);
        verify(bot).executeMessageWithLogging(argThat((SendMessage message) ->
                message.getText().equals(expectedEkateringburgForecast)));
        verify(bot, never()).executeMessageWithLogging(argThat((SendMessage message) ->
                message.getText().equals(expectedNizhnyNovgorodForecast)));

        executorService.elapse(1, TimeUnit.DAYS);
        verify(bot, times(2)).executeMessageWithLogging(argThat((SendMessage message) ->
                message.getText().equals(expectedEkateringburgForecast)));
        verify(bot).executeMessageWithLogging(argThat((SendMessage message) ->
                message.getText().equals(expectedNizhnyNovgorodForecast)));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> reminderService.addReminder(chatId, "Прага", "abc"));
        assertEquals("Wrong time provided!", exception.getMessage());
    }

    /**
     * Проверяет удаление напоминания прогноза погоды.<br>
     * Проверки:
     * <ul>
     *     <li>если указано корретная позиция для удаления, то напоминание должно быть удалено и больше не должно
     *     присылаться</li>
     *     <li>если указана некорректная позиция для удаления, то должно быть выброшено исключение</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест удаления напоминания")
    void testDeleteReminder() {
        long chatId = 1L;
        String placeName = "Екатеринбург";
        LocalTime now = LocalTime.now(ZoneOffset.UTC);
        int deltaInMinutes = 10;
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setChatId(chatId);
        reminder.setPlaceName(placeName);
        reminder.setTime(now.minusMinutes(deltaInMinutes));
        when(reminderRepository.save(any(Reminder.class))).thenReturn(reminder);
        when(reminderRepository.findAllByChatId(chatId)).thenReturn(List.of(reminder));
        reminderService.addReminder(
                chatId,
                placeName,
                now
                        .minusMinutes(deltaInMinutes)
                        .format(DateTimeFormatter.ISO_LOCAL_TIME));

        reminderService.deleteReminderByRelativePosition(chatId, 1);
        executorService.elapse(deltaInMinutes, TimeUnit.MINUTES);
        verify(bot, never()).executeMessageWithLogging(any());

        executorService.elapse(1, TimeUnit.DAYS);
        verify(bot, never()).executeMessageWithLogging(any());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                reminderService.deleteReminderByRelativePosition(chatId, -1));
        assertEquals("Wrong reminder position provided!", exception.getMessage());
    }

}