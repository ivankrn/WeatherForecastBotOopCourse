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
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тесты обработчика сообщений
 */
@ExtendWith(MockitoExtension.class)
class MessageHandlerImplTest {

    private WeatherForecastFormatter forecastFormatter;
    @Mock
    private WeatherForecastService weatherService;
    private MessageHandler messageHandler;
    private Message userMessage;

    @BeforeEach
    void setUp() {
        // TODO: 05.11.2023 Спросить, можно ли не мокать одну из зависимостей, а использовать её реализацию
        forecastFormatter = new WeatherForecastFormatterImpl();
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
        List<WeatherForecast> ekaterinburgTodayForecast = new ArrayList<>(hours);
        for (int hour = 0; hour < hours; hour++) {
            ekaterinburgTodayForecast.add(
                    new WeatherForecast(today.withHour(hour), 0, 0, 740, 70));
        }
        Mockito.when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(ekaterinburgTodayForecast);
        userMessage.setText("/info Екатеринбург");
        List<WeatherForecast> todayForecast = weatherService.getForecast("Екатеринбург", 1);
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
        assertEquals(BotText.WRONG_COMMAND.text, responseMessage.getText());
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

    // TODO: 05.11.2023 Добавить тесты для прогноза на неделю, команд /start и /help
}