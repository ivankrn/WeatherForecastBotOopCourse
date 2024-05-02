package ru.urfu.weatherforecastbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.WeatherForecastBot;
import ru.urfu.weatherforecastbot.database.ReminderRepository;
import ru.urfu.weatherforecastbot.model.Place;
import ru.urfu.weatherforecastbot.model.Reminder;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Тесты сервиса для управления напоминаниями
 */
@ExtendWith(MockitoExtension.class)
class ReminderServiceImplTest {

    /**
     * Сервис для получения прогнозов погоды
     */
    private final WeatherForecastService weatherService = Mockito.mock();
    /**
     * Форматировщик прогнозов погоды
     */
    private final WeatherForecastFormatter forecastFormatter = new WeatherForecastFormatterImpl();
    /**
     * Бот
     */
    @Mock
    private WeatherForecastBot bot;
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
     *     <li>прогноз присылается раз в сутки, не чаще и не реже</li>
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
                    new WeatherForecast(
                            ekateringburg, today.withHour(hour).withMinute(0), 0, 0));
            nizhnyNovgorodForecast.add(
                    new WeatherForecast(
                            nizhnyNovgorod, today.withHour(hour).withMinute(0), 10, 5));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(ekateringburgForecast);
        when(weatherService.getForecast("Нижний Новгород", 1)).thenReturn(nizhnyNovgorodForecast);
        String expectedEkateringburgForecast = """
                🌡️ Прогноз погоды на сегодня (Екатеринбург):
                                
                00-00: 0.0°C (по ощущению 0.0°C)
                01-00: 0.0°C (по ощущению 0.0°C)
                02-00: 0.0°C (по ощущению 0.0°C)
                03-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                05-00: 0.0°C (по ощущению 0.0°C)
                06-00: 0.0°C (по ощущению 0.0°C)
                07-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                09-00: 0.0°C (по ощущению 0.0°C)
                10-00: 0.0°C (по ощущению 0.0°C)
                11-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                13-00: 0.0°C (по ощущению 0.0°C)
                14-00: 0.0°C (по ощущению 0.0°C)
                15-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                17-00: 0.0°C (по ощущению 0.0°C)
                18-00: 0.0°C (по ощущению 0.0°C)
                19-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                21-00: 0.0°C (по ощущению 0.0°C)
                22-00: 0.0°C (по ощущению 0.0°C)
                23-00: 0.0°C (по ощущению 0.0°C)""";
        String expectedNizhnyNovgorodForecast = """
                🌡️ Прогноз погоды на сегодня (Нижний Новгород):
                                
                00-00: 10.0°C (по ощущению 5.0°C)
                01-00: 10.0°C (по ощущению 5.0°C)
                02-00: 10.0°C (по ощущению 5.0°C)
                03-00: 10.0°C (по ощущению 5.0°C)
                04-00: 10.0°C (по ощущению 5.0°C)
                05-00: 10.0°C (по ощущению 5.0°C)
                06-00: 10.0°C (по ощущению 5.0°C)
                07-00: 10.0°C (по ощущению 5.0°C)
                08-00: 10.0°C (по ощущению 5.0°C)
                09-00: 10.0°C (по ощущению 5.0°C)
                10-00: 10.0°C (по ощущению 5.0°C)
                11-00: 10.0°C (по ощущению 5.0°C)
                12-00: 10.0°C (по ощущению 5.0°C)
                13-00: 10.0°C (по ощущению 5.0°C)
                14-00: 10.0°C (по ощущению 5.0°C)
                15-00: 10.0°C (по ощущению 5.0°C)
                16-00: 10.0°C (по ощущению 5.0°C)
                17-00: 10.0°C (по ощущению 5.0°C)
                18-00: 10.0°C (по ощущению 5.0°C)
                19-00: 10.0°C (по ощущению 5.0°C)
                20-00: 10.0°C (по ощущению 5.0°C)
                21-00: 10.0°C (по ощущению 5.0°C)
                22-00: 10.0°C (по ощущению 5.0°C)
                23-00: 10.0°C (по ощущению 5.0°C)""";

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

        verify(bot, never()).sendMessage(eq(chatId), argThat((BotMessage message) ->
                message.getText().equals(expectedEkateringburgForecast)
                        || message.getText().equals(expectedNizhnyNovgorodForecast)));

        executorService.elapse(deltaInMinutes, TimeUnit.MINUTES);
        verify(bot).sendMessage(eq(chatId), argThat((BotMessage message) ->
                message.getText().equals(expectedEkateringburgForecast)));
        verify(bot, never()).sendMessage(eq(chatId), argThat((BotMessage message) ->
                message.getText().equals(expectedNizhnyNovgorodForecast)));

        executorService.elapse(1, TimeUnit.DAYS);
        verify(bot, times(2)).sendMessage(eq(chatId), argThat((BotMessage message) ->
                message.getText().equals(expectedEkateringburgForecast)));
        verify(bot).sendMessage(eq(chatId), argThat((BotMessage message) ->
                message.getText().equals(expectedNizhnyNovgorodForecast)));

        Exception exception = assertThrows(DateTimeParseException.class,
                () -> reminderService.addReminder(chatId, "Прага", "abc"));
        assertEquals("Text 'abc' could not be parsed at index 0", exception.getMessage());
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
        long reminderId = 1L;
        Reminder reminder = new Reminder();
        reminder.setId(reminderId);
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
        verify(reminderRepository).deleteById(reminderId);
        executorService.elapse(deltaInMinutes, TimeUnit.MINUTES);
        verify(bot, never()).sendMessage(eq(chatId), any());

        executorService.elapse(1, TimeUnit.DAYS);
        verify(bot, never()).sendMessage(eq(chatId), any());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                reminderService.deleteReminderByRelativePosition(chatId, -1));
        assertEquals("Wrong reminder position provided!", exception.getMessage());
    }

    /**
     * Проверяет корректное редактирование существующего напоминания пользователя.<br>
     * Проверки:
     * <ul>
     *     <li>Если пользователь вызывает метод редактирования напоминания,
     *     то соответствующее напоминание должно быть изменено.</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест редактирования напоминания")
    void testEditReminder() {
        long chatId = 1L;
        Reminder reminder = new Reminder();
        reminder.setId(1L);
        reminder.setChatId(chatId);
        reminder.setPlaceName("Екатеринбург");
        reminder.setTime(LocalTime.of(5, 0));
        when(reminderRepository.findAllByChatId(chatId)).thenReturn(List.of(reminder));
        when(reminderRepository.save(any())).thenReturn(reminder);
        reminderService.addReminder(chatId, "Екатеринбург", "05:00");

        reminderService.editReminderByRelativePosition(chatId, 1, "Москва", "10:00");

        assertEquals(chatId, reminder.getChatId());
        assertEquals("Москва", reminder.getPlaceName());
        assertEquals(LocalTime.of(10, 0), reminder.getTime());
    }
}