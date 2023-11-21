package ru.urfu.weatherforecastbot.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты обработчика сообщений
 */
@ExtendWith(MockitoExtension.class)
class MessageHandlerImplTest {

    /**
     * Форматировщик прогноза погоды в удобочитаемый вид
     */
    @Mock
    private WeatherForecastFormatter forecastFormatter;
    /**
     * Сервис для получения прогнозов погоды
     */
    @Mock
    private WeatherForecastService weatherService;
    /**
     * Обработчик сообщений
     */
    @InjectMocks
    private MessageHandlerImpl messageHandler;
    /**
     * Сообщение, пришедшее от пользователя и которое требуется обработать
     */
    private Message userMessage;

    /**
     * Подготавливает окружение перед тестами
     */
    @BeforeEach
    void setUp() {
        userMessage = new Message();
        Chat userChat = new Chat();
        long userChatId = 1L;
        userChat.setId(userChatId);
        userMessage.setChat(userChat);
    }

    @Test
    @DisplayName("Бот должен отвечать именно тому пользователю, от которого пришло сообщение")
    void whenUserSendsMessage_thenSendMessageToThatUser() {
        userMessage.setText("/start");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals(userMessage.getChatId(), Long.parseLong(responseMessage.getChatId()));
    }

    @Test
    @DisplayName("При запросе прогноза погоды на сегодня в определенном месте ответное сообщение должно содержать прогноз " +
            "погоды на сегодня в том же самом определенном месте")
    void givenPlace_whenTodayForecast_thenReturnTodayForecastForThatPlace() {
        LocalDateTime today = LocalDateTime.now();
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
        when(forecastFormatter.formatTodayForecast(todayForecast))
                .thenReturn("Прогноз погоды на сегодня (Екатеринбург): ...");
        userMessage.setText("/info Екатеринбург");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Прогноз погоды на сегодня (Екатеринбург): ...", responseMessage.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на сегодня без указания места ответное сообщение должно содержать " +
            "предупреждение о том, что команда введена неверно")
    void givenNoPlaceName_whenTodayForecast_thenReturnWrongCommand() {
        userMessage.setText("/info");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Команда введена неверно, попробуйте ещё раз.", responseMessage.getText());
    }

    @Test
    @DisplayName("Если не удается найти указанное место,то ответное сообщение должно содержать " +
            "предупреждение о том, что место не найдено")
    void givenNotFoundPlace_whenTodayForecast_thenReturnNotFound() {
        userMessage.setText("/info там_где_нас_нет");
        when(weatherService.getForecast("там_где_нас_нет", 1)).thenReturn(null);

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Извините, данное место не найдено.", responseMessage.getText());
    }

    @Test
    @DisplayName("При вводе неизвестной команды ответное сообщение должно содержать предупреждение о том, что" +
            "бот не знает такой команды")
    void givenUnknownCommand_thenReturnUnknownCommand() {
        userMessage.setText("/some_unknown_command");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Извините, я не знаю такой команды.", responseMessage.getText());
    }

    @Test
    @DisplayName("При взаимодействии с несколькими пользователями, бот должен отвечать каждому пользователю " +
            "соответственно")
    void givenSeveralUsers_thenAnswerEveryone() {
        Chat marsDwellerChat = new Chat();
        long marsDwellerChatId = 1L;
        marsDwellerChat.setId(marsDwellerChatId);
        Message marsDwellerMessage = new Message();
        marsDwellerMessage.setText("/info Марс");
        marsDwellerMessage.setChat(marsDwellerChat);
        Chat instructionsBookwormChat = new Chat();
        long instructionsBookwormChatId = 2L;
        instructionsBookwormChat.setId(instructionsBookwormChatId);
        Message instructionsBookwormMessage = new Message();
        instructionsBookwormMessage.setText("/some_unknown_command Москва");
        instructionsBookwormMessage.setChat(instructionsBookwormChat);
        Chat typicalUserChat = new Chat();
        long typicalUserChatId = 3L;
        typicalUserChat.setId(typicalUserChatId);
        Message typicalUserMessage = new Message();
        typicalUserMessage.setText("/info Москва");
        typicalUserMessage.setChat(typicalUserChat);
        LocalDateTime today = LocalDateTime.now();
        int hours = 24;
        List<WeatherForecast> marsTodayForecast = new ArrayList<>(hours);
        List<WeatherForecast> moscowTodayForecast = new ArrayList<>(hours);
        for (int hour = 0; hour < hours; hour++) {
            moscowTodayForecast.add(
                    new WeatherForecast(today.withHour(hour), 10, 5));
            marsTodayForecast.add(
                    new WeatherForecast(today.withHour(hour), -60, -60));
        }
        when(weatherService.getForecast("Марс", 1))
                .thenReturn(marsTodayForecast);
        when(weatherService.getForecast("Москва", 1))
                .thenReturn(moscowTodayForecast);
        when(forecastFormatter.formatTodayForecast(marsTodayForecast))
                .thenReturn("Прогноз погоды на сегодня (Марс): ...");
        when(forecastFormatter.formatTodayForecast(moscowTodayForecast))
                .thenReturn("Прогноз погоды на сегодня (Москва): ...");

        SendMessage replyToMarsDweller = messageHandler.handle(marsDwellerMessage);
        SendMessage replyToInstructionsBookworm = messageHandler.handle(instructionsBookwormMessage);
        SendMessage replyToTypicalUser = messageHandler.handle(typicalUserMessage);

        assertEquals(marsDwellerChatId, Long.parseLong(replyToMarsDweller.getChatId()));
        assertEquals("Прогноз погоды на сегодня (Марс): ...", replyToMarsDweller.getText());
        assertEquals(instructionsBookwormChatId, Long.parseLong(replyToInstructionsBookworm.getChatId()));
        assertEquals("Извините, я не знаю такой команды.", replyToInstructionsBookworm.getText());
        assertEquals(typicalUserChatId, Long.parseLong(replyToTypicalUser.getChatId()));
        assertEquals("Прогноз погоды на сегодня (Москва): ...", replyToTypicalUser.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на неделю вперед в определенном месте ответное " +
            "сообщение должно содержать прогноз погоды на неделю вперед в том же самом определенном месте")
    void givenPlace_whenWeekForecast_thenReturnFormattedWeekForecast() {
        LocalDateTime now = LocalDateTime.now();
        int days = 7;
        int hourInterval = 4;
        List<WeatherForecast> weekForecast = new ArrayList<>();
        for (int day = 0; day < days; day++) {
            for (int hour = 0; hour < 24; hour += hourInterval) {
                weekForecast.add(
                        new WeatherForecast(now.plusDays(day).withHour(hour), 0, 0));
            }
        }
        when(weatherService.getForecast("Екатеринбург", 7))
                .thenReturn(weekForecast);
        when(forecastFormatter.formatWeekForecast(weekForecast))
                .thenReturn("Прогноз погоды на неделю вперед (Екатеринбург): ...");
        userMessage.setText("/info_week Екатеринбург");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Прогноз погоды на неделю вперед (Екатеринбург): ...", responseMessage.getText());
        verify(forecastFormatter).formatWeekForecast(weekForecast);
    }

    @Test
    @DisplayName("При запросе прогноза погоды на неделю вперед для ненайденного города " +
            "должно возвращаться сообщение об ошибке")
    void givenNonexistentPlace_whenWeekForecast_thenErrorMessage() {
        userMessage.setText("/info_week там_где_нас_нет");

        when(weatherService.getForecast("там_где_нас_нет", 7)).thenReturn(null);

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Извините, данное место не найдено.", responseMessage.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на неделю вперед без указания города " +
            "должно возвращаться сообщение об ошибке")
    void givenNoPlaceName_whenWeekForecast_thenReturnWrongCommand() {
        userMessage.setText("/info_week");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Команда введена неверно, попробуйте ещё раз.", responseMessage.getText());
    }

    @Test
    @DisplayName("При вводе команды \"/start\" пользователю должно отобразиться приветствие")
    void givenStartCommand_thenReturnHelloMessage() {
        userMessage.setText("/start");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("""
                        Здравствуйте! Я бот для просмотра прогноза погоды. Доступны следующие команды:
                        /start - запустить бота
                        /help - меню помощи
                        /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                        /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
                        """,
                responseMessage.getText());
    }

    @Test
    @DisplayName("При вводе команды \"/help\" пользователю должно отобразиться сообщение помощи")
    void givenHelpCommand_thenReturnHelpMessage() {
        userMessage.setText("/help");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("""
                        Вы зашли в меню помощи. Для вас доступны следующие команды:
                        /start - запустить бота
                        /help - меню помощи
                        /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                        /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
                        """,
                responseMessage.getText());
    }
}