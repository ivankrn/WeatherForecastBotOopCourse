package ru.urfu.weatherforecastbot.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private MessageHandler messageHandler;
    /**
     * Сообщение, пришедшее от пользователя и которое требуется обработать
     */
    private Message userMessage;

    /**
     * Подготавливает окружение перед тестами
     */
    @BeforeEach
    void setUp() {
        messageHandler = new MessageHandlerImpl(weatherService, forecastFormatter);
        userMessage = new Message();
        Chat userChat = new Chat();
        long userChatId = 1L;
        userChat.setId(userChatId);
        userMessage.setChat(userChat);
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
        Mockito.when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
        Mockito.when(forecastFormatter.formatTodayForecast(todayForecast))
                .thenReturn("Прогноз погоды на сегодня: ...");
        userMessage.setText("/info Екатеринбург");
        String expectedTodayForecast = forecastFormatter.formatTodayForecast(todayForecast);

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals(userMessage.getChatId(), Long.parseLong(responseMessage.getChatId()));
        assertEquals(expectedTodayForecast, responseMessage.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на сегодня без указания места ответное сообщение должно содержать " +
            "предупреждение о том, что команда введена неверно")
    void givenNoPlaceName_whenTodayForecast_thenReturnWrongCommand() {
        userMessage.setText("/info");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals(userMessage.getChatId(), Long.parseLong(responseMessage.getChatId()));
        assertEquals(BotText.WRONG_COMMAND_SYNTAX.text, responseMessage.getText());
    }

    @Test
    @DisplayName("Если не удается найти указанное место,то ответное сообщение должно содержать " +
            "предупреждение о том, что место не найдено")
    void givenNotFoundPlace_whenTodayForecast_thenReturnNotFound() {
        userMessage.setText("/info там_где_нас_нет");
        Mockito.when(weatherService.getForecast("там_где_нас_нет", 1)).thenReturn(null);

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals(userMessage.getChatId(), Long.parseLong(responseMessage.getChatId()));
        assertEquals(BotText.NOT_FOUND.text, responseMessage.getText());
    }

    @Test
    @DisplayName("При вводе неизвестной команды ответное сообщение должно содержать предупреждение о том, что" +
            "бот не знает такой команды")
    void givenUnknownCommand_thenReturnUnknownCommand() {
        userMessage.setText("/some_unknown_command");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals(userMessage.getChatId(), Long.parseLong(responseMessage.getChatId()));
        assertEquals(BotText.UNKNOWN_COMMAND.text, responseMessage.getText());
    }

    @Test
    @DisplayName("При вводе нескольких последовательных команд, бот должен отвечать в том же порядке, в котором " +
            "пришли исходные сообщения")
    void givenCommandSequence_thenAnswerInSameOrder() {
        Chat userChat = new Chat();
        long userChatId = 1L;
        userChat.setId(userChatId);
        Message firstMessage = new Message();
        firstMessage.setText("/pogoda Москва");
        firstMessage.setChat(userChat);
        Message secondMessage = new Message();
        secondMessage.setText("/info Москва");
        secondMessage.setChat(userChat);
        Message thirdMessage = new Message();
        thirdMessage.setText("/info Екатеринбург");
        thirdMessage.setChat(userChat);
        LocalDateTime today = LocalDateTime.now();
        int hours = 24;
        List<WeatherForecast> moscowTodayForecast = new ArrayList<>(hours);
        List<WeatherForecast> ekaterinburgTodayForecast = new ArrayList<>(hours);
        for (int hour = 0; hour < hours; hour++) {
            moscowTodayForecast.add(
                    new WeatherForecast(today.withHour(hour), 10, 5));
            ekaterinburgTodayForecast.add(
                    new WeatherForecast(today.withHour(hour), 0, 0));
        }
        Mockito.when(weatherService.getForecast("Москва", 1))
                .thenReturn(moscowTodayForecast);
        Mockito.when(weatherService.getForecast("Екатеринбург", 1))
                .thenReturn(ekaterinburgTodayForecast);
        Mockito.when(forecastFormatter.formatTodayForecast(moscowTodayForecast))
                .thenReturn("Прогноз погоды на сегодня (Москва): ...");
        Mockito.when(forecastFormatter.formatTodayForecast(ekaterinburgTodayForecast))
                .thenReturn("Прогноз погоды на сегодня (Екатеринбург): ...");
        String expectedMoscowForecast = forecastFormatter.formatTodayForecast(moscowTodayForecast);
        String expectedEkaterinburgForecast = forecastFormatter.formatTodayForecast(ekaterinburgTodayForecast);

        SendMessage replyToFirstMessage = messageHandler.handle(firstMessage);
        SendMessage replyToSecondMessage = messageHandler.handle(secondMessage);
        SendMessage replyToThirdMessage = messageHandler.handle(thirdMessage);

        assertEquals(userChatId, Long.parseLong(replyToFirstMessage.getChatId()));
        assertEquals(BotText.UNKNOWN_COMMAND.text, replyToFirstMessage.getText());
        assertEquals(userChatId, Long.parseLong(replyToSecondMessage.getChatId()));
        assertEquals(expectedMoscowForecast, replyToSecondMessage.getText());
        assertEquals(userChatId, Long.parseLong(replyToThirdMessage.getChatId()));
        assertEquals(expectedEkaterinburgForecast, replyToThirdMessage.getText());
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
        Mockito.when(weatherService.getForecast("Марс", 1))
                .thenReturn(marsTodayForecast);
        Mockito.when(weatherService.getForecast("Москва", 1))
                .thenReturn(moscowTodayForecast);
        Mockito.when(forecastFormatter.formatTodayForecast(marsTodayForecast))
                .thenReturn("Прогноз погоды на сегодня (Марс): ...");
        Mockito.when(forecastFormatter.formatTodayForecast(moscowTodayForecast))
                .thenReturn("Прогноз погоды на сегодня (Москва): ...");
        String expectedMarsForecast = forecastFormatter.formatTodayForecast(marsTodayForecast);
        String expectedMoscowForecast = forecastFormatter.formatTodayForecast(moscowTodayForecast);

        SendMessage replyToMarsDweller = messageHandler.handle(marsDwellerMessage);
        SendMessage replyToInstructionsBookworm = messageHandler.handle(instructionsBookwormMessage);
        SendMessage replyToTypicalUser = messageHandler.handle(typicalUserMessage);

        assertEquals(marsDwellerChatId, Long.parseLong(replyToMarsDweller.getChatId()));
        assertEquals(expectedMarsForecast, replyToMarsDweller.getText());
        assertEquals(instructionsBookwormChatId, Long.parseLong(replyToInstructionsBookworm.getChatId()));
        assertEquals(BotText.UNKNOWN_COMMAND.text, replyToInstructionsBookworm.getText());
        assertEquals(typicalUserChatId, Long.parseLong(replyToTypicalUser.getChatId()));
        assertEquals(expectedMoscowForecast, replyToTypicalUser.getText());
    }
    // TODO: 05.11.2023 Добавить тесты для прогноза на неделю, команд /start и /help
}