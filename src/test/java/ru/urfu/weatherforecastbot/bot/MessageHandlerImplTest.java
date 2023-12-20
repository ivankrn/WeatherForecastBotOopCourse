package ru.urfu.weatherforecastbot.bot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.urfu.weatherforecastbot.bot.state.BotState;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.database.ChatStateRepository;
import ru.urfu.weatherforecastbot.model.*;
import ru.urfu.weatherforecastbot.service.ReminderService;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Тесты обработчика сообщений
 */
@ExtendWith(MockitoExtension.class)
class MessageHandlerImplTest {

    /**
     * Форматировщик прогноза погоды в удобочитаемый вид
     */
    private final WeatherForecastFormatter forecastFormatter = new WeatherForecastFormatterImpl();
    /**
     * Сервис для получения прогнозов погоды
     */
    private final WeatherForecastService weatherService;
    /**
     * Репозиторий контекстов чатов
     */
    private final ChatContextRepository chatContextRepository;
    /**
     * Репозиторий состояний чатов
     */
    private final ChatStateRepository chatStateRepository;
    /**
     * Обработчик сообщений
     */
    private final MessageHandler messageHandler;
    /**
     * Сервис для управления напоминаниями
     */
    private final ReminderService reminderService;

    public MessageHandlerImplTest(@Mock WeatherForecastService weatherService,
                                  @Mock ChatContextRepository chatContextRepository,
                                  @Mock ChatStateRepository chatStateRepository,
                                  @Mock ReminderService reminderService) {
        this.weatherService = weatherService;
        this.chatContextRepository = chatContextRepository;
        this.chatStateRepository = chatStateRepository;
        this.reminderService = reminderService;
        messageHandler = new MessageHandlerImpl(weatherService, forecastFormatter,
                chatContextRepository, chatStateRepository, reminderService);
    }

    @Test
    @DisplayName("При запросе прогноза погоды на сегодня в определенном месте ответное сообщение должно содержать прогноз " +
            "погоды на сегодня в том же самом определенном месте")
    void givenPlace_whenTodayForecast_thenReturnTodayForecastForThatPlace() {
        LocalDateTime today = LocalDateTime.of(2023, 10, 10, 0, 0);
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);

        BotMessage responseMessage = messageHandler.handle(1L, "/info Екатеринбург");

        assertEquals("""
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
                23-00: 0.0°C (по ощущению 0.0°C)""", responseMessage.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на сегодня без указания места бот запросит название места, после чего " +
            "отправит прогноз, при этом временной период не будет запрошен")
    void givenNoPlaceName_whenTodayForecast_thenAskOnlyPlace() {
        long chatId = 1L;
        LocalDateTime today = LocalDateTime.of(2023, 10, 10, 0, 0);
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
        ChatContext chatContext = new ChatContext();
        chatContext.setChatId(chatId);
        when(chatContextRepository.findById(chatId)).thenReturn(Optional.of(chatContext));
        ChatState chatState = new ChatState();
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        BotMessage forecastTodayMessageResponse = messageHandler.handle(chatId, "/info");
        assertEquals("Введите название места", forecastTodayMessageResponse.getText());
        assertEquals(BotState.WAITING_FOR_TODAY_FORECAST_PLACE_NAME, chatState.getBotState());
        List<Button> forecastTodayMessageButtons = forecastTodayMessageResponse.getButtons();
        assertEquals(1, forecastTodayMessageButtons.size());
        assertEquals("Отмена", forecastTodayMessageButtons.get(0).getText());
        assertEquals("/cancel", forecastTodayMessageButtons.get(0).getCallback());

        BotMessage placeNameMessageResponse = messageHandler.handle(chatId, "Екатеринбург");
        assertEquals("""
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
                23-00: 0.0°C (по ощущению 0.0°C)""", placeNameMessageResponse.getText());
        assertEquals(BotState.INITIAL, chatState.getBotState());
    }

    @Test
    @DisplayName("Если не удается найти указанное место, то ответное сообщение должно содержать " +
            "предупреждение о том, что место не найдено")
    void givenNotFoundPlace_whenTodayForecast_thenReturnNotFound() {
        when(weatherService.getForecast("там_где_нас_нет", 1)).thenReturn(List.of());

        BotMessage responseMessage = messageHandler.handle(1L, "/info там_где_нас_нет");

        assertEquals("Извините, данное место не найдено.", responseMessage.getText());
    }

    @Test
    @DisplayName("При вводе неизвестной команды ответное сообщение должно содержать предупреждение о том, что " +
            "бот не знает такой команды")
    void givenUnknownCommand_thenReturnUnknownCommand() {
        long chatId = 1L;
        ChatContext chatContext = new ChatContext();
        chatContext.setChatId(chatId);
        when(chatContextRepository.findById(chatId)).thenReturn(Optional.of(chatContext));
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        BotMessage responseMessage = messageHandler.handle(chatId, "/some_unknown_command");

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
        typicalUserMessage.setText("/info Нижний Новгород");
        typicalUserMessage.setChat(typicalUserChat);
        LocalDateTime today = LocalDateTime.of(2023, 10, 10, 0, 0);
        int hours = 24;
        Place mars = new Place("Марс", 0, 0, "Mars/Mars");
        Place nizhnyNovgorod = new Place("Нижний Новгород", 56.328, 44.002, "Europe/Moscow");
        List<WeatherForecast> nizhnyNovgorodTodayForecast = new ArrayList<>(hours);
        List<WeatherForecast> marsTodayForecast = new ArrayList<>(hours);
        for (int hour = 0; hour < hours; hour++) {
            marsTodayForecast.add(
                    new WeatherForecast(mars, today.withHour(hour), -60, -60));
            nizhnyNovgorodTodayForecast.add(
                    new WeatherForecast(nizhnyNovgorod, today.withHour(hour), 10, 5));
        }
        when(weatherService.getForecast("Марс", 1))
                .thenReturn(marsTodayForecast);
        when(weatherService.getForecast("Нижний Новгород", 1))
                .thenReturn(nizhnyNovgorodTodayForecast);
        ChatContext marsDwellerChatContext = new ChatContext();
        marsDwellerChatContext.setChatId(marsDwellerChatId);
        when(chatContextRepository.findById(marsDwellerChatId))
                .thenReturn(Optional.of(marsDwellerChatContext));
        ChatState marsDwellerChatState = new ChatState();
        marsDwellerChatState.setChatId(marsDwellerChatId);
        marsDwellerChatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(marsDwellerChatId)).thenReturn(Optional.of(marsDwellerChatState));
        ChatContext instructionsBookwormChatContext = new ChatContext();
        instructionsBookwormChatContext.setChatId(instructionsBookwormChatId);
        when(chatContextRepository.findById(instructionsBookwormChatId))
                .thenReturn(Optional.of(instructionsBookwormChatContext));
        ChatState instructionsBookwormChatState = new ChatState();
        instructionsBookwormChatState.setChatId(instructionsBookwormChatId);
        instructionsBookwormChatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(instructionsBookwormChatId))
                .thenReturn(Optional.of(instructionsBookwormChatState));
        ChatContext typicalUserChatContext = new ChatContext();
        typicalUserChatContext.setChatId(typicalUserChatId);
        when(chatContextRepository.findById(typicalUserChatId))
                .thenReturn(Optional.of(typicalUserChatContext));
        ChatState typicalUserChatState = new ChatState();
        typicalUserChatState.setChatId(typicalUserChatId);
        typicalUserChatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(typicalUserChatId)).thenReturn(Optional.of(typicalUserChatState));

        BotMessage replyToMarsDweller = messageHandler.handle(marsDwellerChatId, "/info Марс");
        BotMessage replyToInstructionsBookworm =
                messageHandler.handle(instructionsBookwormChatId, "/some_unknown_command Москва");
        BotMessage replyToTypicalUser = messageHandler.handle(typicalUserChatId, "/info Нижний Новгород");

        assertEquals("""
                🌡️ Прогноз погоды на сегодня (Марс):
                                
                00-00: -60.0°C (по ощущению -60.0°C)
                01-00: -60.0°C (по ощущению -60.0°C)
                02-00: -60.0°C (по ощущению -60.0°C)
                03-00: -60.0°C (по ощущению -60.0°C)
                04-00: -60.0°C (по ощущению -60.0°C)
                05-00: -60.0°C (по ощущению -60.0°C)
                06-00: -60.0°C (по ощущению -60.0°C)
                07-00: -60.0°C (по ощущению -60.0°C)
                08-00: -60.0°C (по ощущению -60.0°C)
                09-00: -60.0°C (по ощущению -60.0°C)
                10-00: -60.0°C (по ощущению -60.0°C)
                11-00: -60.0°C (по ощущению -60.0°C)
                12-00: -60.0°C (по ощущению -60.0°C)
                13-00: -60.0°C (по ощущению -60.0°C)
                14-00: -60.0°C (по ощущению -60.0°C)
                15-00: -60.0°C (по ощущению -60.0°C)
                16-00: -60.0°C (по ощущению -60.0°C)
                17-00: -60.0°C (по ощущению -60.0°C)
                18-00: -60.0°C (по ощущению -60.0°C)
                19-00: -60.0°C (по ощущению -60.0°C)
                20-00: -60.0°C (по ощущению -60.0°C)
                21-00: -60.0°C (по ощущению -60.0°C)
                22-00: -60.0°C (по ощущению -60.0°C)
                23-00: -60.0°C (по ощущению -60.0°C)""", replyToMarsDweller.getText());
        assertEquals("Извините, я не знаю такой команды.", replyToInstructionsBookworm.getText());
        // проверяем, что корректно работает с названиями, содержащие пробелы
        assertEquals("""
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
                23-00: 10.0°C (по ощущению 5.0°C)""", replyToTypicalUser.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на неделю вперед в определенном месте ответное " +
            "сообщение должно содержать прогноз погоды на неделю вперед в том же самом определенном месте")
    void givenPlace_whenWeekForecast_thenReturnFormattedWeekForecast() {
        LocalDateTime now = LocalDateTime.of(2023, 10, 10, 0, 0);
        int days = 7;
        int hourInterval = 4;
        List<WeatherForecast> weekForecast = new ArrayList<>();
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int day = 0; day < days; day++) {
            for (int hour = 0; hour < 24; hour += hourInterval) {
                weekForecast.add(
                        new WeatherForecast(place, now.plusDays(day).withHour(hour), 0, 0));
            }
        }
        when(weatherService.getForecast("Екатеринбург", 7))
                .thenReturn(weekForecast);

        BotMessage responseMessage = messageHandler.handle(1L, "/info_week Екатеринбург");

        assertEquals("""
                🌡️ Прогноз погоды на неделю (Екатеринбург):
                                
                10.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                11.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                12.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                13.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                14.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                15.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                16.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)""", responseMessage.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на неделю вперед для ненайденного города " +
            "должно возвращаться сообщение об ошибке")
    void givenNonExistentPlace_whenWeekForecast_thenErrorMessage() {
        when(weatherService.getForecast("там_где_нас_нет", 7)).thenReturn(List.of());

        BotMessage responseMessage = messageHandler.handle(1L, "/info_week там_где_нас_нет");

        assertEquals("Извините, данное место не найдено.", responseMessage.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на неделю вперед без указания города бот запросит название места, после " +
            "чего отправит прогноз погоды, при этом временной период не будет запрошен")
    void givenNoPlaceName_whenWeekForecast_thenAskOnlyPlace() {
        long chatId = 1L;
        LocalDateTime now = LocalDateTime.of(2023, 10, 10, 0, 0);
        int days = 7;
        int hourInterval = 4;
        List<WeatherForecast> weekForecast = new ArrayList<>();
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int day = 0; day < days; day++) {
            for (int hour = 0; hour < 24; hour += hourInterval) {
                weekForecast.add(
                        new WeatherForecast(place, now.plusDays(day).withHour(hour), 0, 0));
            }
        }
        when(weatherService.getForecast("Екатеринбург", 7)).thenReturn(weekForecast);
        ChatContext chatContext = new ChatContext();
        chatContext.setChatId(chatId);
        when(chatContextRepository.findById(chatId)).thenReturn(Optional.of(chatContext));
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        BotMessage forecastWeekMessageResponse = messageHandler.handle(chatId, "/info_week");
        assertEquals("Введите название места", forecastWeekMessageResponse.getText());
        assertEquals(BotState.WAITING_FOR_WEEK_FORECAST_PLACE_NAME, chatState.getBotState());
        List<Button> forecastWeekMessageButtons = forecastWeekMessageResponse.getButtons();
        assertEquals(1, forecastWeekMessageButtons.size());
        assertEquals("Отмена", forecastWeekMessageButtons.get(0).getText());
        assertEquals("/cancel", forecastWeekMessageButtons.get(0).getCallback());

        BotMessage placeNameMessageResponse = messageHandler.handle(chatId, "Екатеринбург");
        assertEquals("""
                🌡️ Прогноз погоды на неделю (Екатеринбург):
                                
                10.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                11.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                12.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                13.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                14.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                15.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                                
                16.10.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)""", placeNameMessageResponse.getText());
        assertEquals(BotState.INITIAL, chatState.getBotState());
    }

    @Test
    @DisplayName("При вводе команды \"/start\" пользователю должно отобразиться приветствие")
    void givenStartCommand_thenReturnHelloMessage() {
        long chatId = 1L;
        ChatContext chatContext = new ChatContext();
        chatContext.setChatId(chatId);
        when(chatContextRepository.findById(chatId)).thenReturn(Optional.of(chatContext));
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        BotMessage responseMessage = messageHandler.handle(chatId, "/start");
        List<Button> responseButtons = responseMessage.getButtons();

        assertEquals("""
                        Здравствуйте! Я бот для просмотра прогноза погоды. Доступны следующие команды:
                        /start - запустить бота
                        /help - меню помощи
                        /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                        /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
                        /subscribe <название населенного пункта> <время по Гринвичу> - создать напоминание прогноза погоды
                        /del_subscription <номер напоминания> - удалить напоминание с указанным номером
                        """,
                responseMessage.getText());
        assertEquals(3, responseButtons.size());
        assertEquals("Узнать прогноз", responseButtons.get(0).getText());
        assertEquals("/forecast", responseButtons.get(0).getCallback());
        assertEquals("Помощь", responseButtons.get(1).getText());
        assertEquals("/help", responseButtons.get(1).getCallback());
        assertEquals("Отмена", responseButtons.get(2).getText());
        assertEquals("/cancel", responseButtons.get(2).getCallback());
    }

    @Test
    @DisplayName("При вводе команды \"/help\" пользователю должно отобразиться сообщение помощи")
    void givenHelpCommand_thenReturnHelpMessage() {
        BotMessage responseMessage = messageHandler.handle(1L, "/help");

        assertEquals("""
                        Вы зашли в меню помощи. Для вас доступны следующие команды:
                        /start - запустить бота
                        /help - меню помощи
                        /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                        /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
                        /subscribe <название населенного пункта> <время по Гринвичу> - создать напоминание прогноза погоды
                        /del_subscription <номер напоминания> - удалить напоминание с указанным номером
                        """,
                responseMessage.getText());
    }

    @Test
    @DisplayName("Если пользователь запрашивает прогноз без указания места и времени, то бот должен последовательно " +
            "уточнить все детали, после чего прислать соответствующий прогноз погоды")
    void whenForecast_thenAskDetails() {
        long chatId = 1L;
        LocalDateTime today = LocalDateTime.of(2023, 10, 10, 0, 0);
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
        ChatContext chatContext = new ChatContext();
        chatContext.setChatId(chatId);
        when(chatContextRepository.findById(chatId)).thenReturn(Optional.of(chatContext));
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        BotMessage forecastMessageResponse = messageHandler.handle(chatId, "/forecast");
        assertEquals("Введите название места", forecastMessageResponse.getText());
        assertEquals(BotState.WAITING_FOR_PLACE_NAME, chatState.getBotState());
        List<Button> forecastMessageButtons = forecastMessageResponse.getButtons();
        assertEquals(1, forecastMessageButtons.size());
        assertEquals("Отмена", forecastMessageButtons.get(0).getText());
        assertEquals("/cancel", forecastMessageButtons.get(0).getCallback());

        BotMessage placeNameMessageResponse = messageHandler.handle(chatId, "Екатеринбург");
        assertEquals("Выберите временной период для просмотра (сегодня, завтра, неделя)",
                placeNameMessageResponse.getText());
        List<Button> placeNameMessageButtons = placeNameMessageResponse.getButtons();
        assertEquals(BotState.WAITING_FOR_TIME_PERIOD, chatState.getBotState());
        assertEquals(4, placeNameMessageButtons.size());
        assertEquals("Сегодня", placeNameMessageButtons.get(0).getText());
        assertEquals("Сегодня", placeNameMessageButtons.get(0).getCallback());
        assertEquals("Завтра", placeNameMessageButtons.get(1).getText());
        assertEquals("Завтра", placeNameMessageButtons.get(1).getCallback());
        assertEquals("Неделя", placeNameMessageButtons.get(2).getText());
        assertEquals("Неделя", placeNameMessageButtons.get(2).getCallback());
        assertEquals("Отмена", placeNameMessageButtons.get(3).getText());
        assertEquals("/cancel", placeNameMessageButtons.get(3).getCallback());

        BotMessage timePeriodMessageResponse = messageHandler.handle(chatId, "Сегодня");
        assertEquals("""
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
                23-00: 0.0°C (по ощущению 0.0°C)""", timePeriodMessageResponse.getText());
        assertEquals(BotState.INITIAL, chatState.getBotState());
    }

    @Test
    @DisplayName("Если пользователь во время запроса погоды присылает некорректный временной период, то ответное " +
            "сообщение должно содержать просьбу ввести временной период повторно")
    void givenUserSendsWrongTimePeriod_whenForecast_thenAskTimePeriodAgain() {
        long chatId = 1L;
        ChatContext chatContext = new ChatContext();
        chatContext.setChatId(chatId);
        when(chatContextRepository.findById(chatId)).thenReturn(Optional.of(chatContext));
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        messageHandler.handle(chatId, "/forecast");
        messageHandler.handle(chatId, "Екатеринбург");
        BotMessage wrongTimePeriodMessageResponse = messageHandler.handle(chatId, "привет");

        assertEquals("Введите корректный временной период. Допустимые значения: сегодня, завтра, неделя",
                wrongTimePeriodMessageResponse.getText());
        List<Button> responseMessageButtons = wrongTimePeriodMessageResponse.getButtons();
        assertEquals(4, responseMessageButtons.size());
        assertEquals("Сегодня", responseMessageButtons.get(0).getText());
        assertEquals("Сегодня", responseMessageButtons.get(0).getCallback());
        assertEquals("Завтра", responseMessageButtons.get(1).getText());
        assertEquals("Завтра", responseMessageButtons.get(1).getCallback());
        assertEquals("Неделя", responseMessageButtons.get(2).getText());
        assertEquals("Неделя", responseMessageButtons.get(2).getCallback());
        assertEquals("Отмена", responseMessageButtons.get(3).getText());
        assertEquals("/cancel", responseMessageButtons.get(3).getCallback());
    }

    @Test
    @DisplayName("Если пользователь отменяет действие, то ответное сообщение должно содержать уведомление о  " +
            "возврате в меню")
    void whenCancel_thenReturnToMenu() {
        long chatId = 1L;
        ChatContext chatContext = new ChatContext();
        chatContext.setChatId(chatId);
        when(chatContextRepository.findById(chatId)).thenReturn(Optional.of(chatContext));
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        messageHandler.handle(chatId, "/info");
        BotMessage responseMessage = messageHandler.handle(chatId, "/cancel");

        assertEquals("Вы вернулись в основное меню", responseMessage.getText());
        List<Button> responseButtons = responseMessage.getButtons();
        assertEquals(3, responseButtons.size());
        assertEquals("Узнать прогноз", responseButtons.get(0).getText());
        assertEquals("/forecast", responseButtons.get(0).getCallback());
        assertEquals("Помощь", responseButtons.get(1).getText());
        assertEquals("/help", responseButtons.get(1).getCallback());
        assertEquals("Отмена", responseButtons.get(2).getText());
        assertEquals("/cancel", responseButtons.get(2).getCallback());
    }

    /**
     * Проверяет полную команду на создание напоминания прогноза.<br>
     * Проверки:
     * <ul>
     *     <li>если пользователь указал корректное время напоминания, то ответное сообщение должно содержать уведомление
     *     о том, что напоминание создано, при этом оно должно создаться</li>
     *     <li>если пользователь указал некорректное время напоминания, то ответное сообщение должно содержать
     *     просьбу ввести время в корректном формате</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест на полную команду создания напоминания")
    void testFullSubscribeCommand() {
        long chatId = 1L;

        BotMessage correctTimeMessageResponse = messageHandler.handle(chatId, "/subscribe Екатеринбург 05:00");
        assertEquals("Напоминание создано. Буду присылать прогноз погоды в 05:00",
                correctTimeMessageResponse.getText());
        verify(reminderService).addReminder(chatId, "Екатеринбург", "05:00");

        doThrow(DateTimeParseException.class).when(reminderService)
                .addReminder(chatId, "Екатеринбург", "abc");

        BotMessage wrongTimeMessageResponse = messageHandler.handle(chatId, "/subscribe Екатеринбург abc");
        assertEquals("Некорректный формат времени. Введите время в виде 00:00 (часы:минуты)",
                wrongTimeMessageResponse.getText());
    }

    /**
     * Проверяет неполную команду на создание напоминания прогноза.<br>
     * Проверки:
     * <ul>
     *     <li>если во время диалога с ботом пользователь присылает некорректное время, то ответное сообщение должно
     *     содержать просьбу ввести время в корректном формате</li>
     *     <li>если во время диалога с ботом пользователь присылает корректное время, то ответное сообщение должно
     *     содержать уведомление о том, что напоминание создано, при этом оно должно создаться</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест на неполную команду создания напоминания")
    void testNotFullSubscribeCommand() {
        long chatId = 1L;
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));
        ChatContext chatContext = new ChatContext();
        chatContext.setChatId(chatId);
        when(chatContextRepository.findById(chatId)).thenReturn(Optional.of(chatContext));

        BotMessage subscribeMessageResponse = messageHandler.handle(chatId, "/subscribe");
        assertEquals("Введите название места, для которого будут присылаться напоминания",
                subscribeMessageResponse.getText());
        assertEquals(1, subscribeMessageResponse.getButtons().size());
        assertEquals("Отмена", subscribeMessageResponse.getButtons().get(0).getText());
        assertEquals("/cancel", subscribeMessageResponse.getButtons().get(0).getCallback());

        BotMessage placeNameMessageResponse = messageHandler.handle(chatId, "Екатеринбург");
        assertEquals("Введите время (в UTC), когда должно присылаться напоминание прогноза (пример: 08:00)",
                placeNameMessageResponse.getText());
        assertEquals(1, placeNameMessageResponse.getButtons().size());
        assertEquals("Отмена", placeNameMessageResponse.getButtons().get(0).getText());
        assertEquals("/cancel", placeNameMessageResponse.getButtons().get(0).getCallback());

        doThrow(DateTimeParseException.class).when(reminderService)
                .addReminder(chatId,
                        "Екатеринбург",
                        "Время - очередная иллюзия, чьим рабом я не желаю быть.");

        BotMessage wrongTimeMessageResponse =
                messageHandler.handle(chatId, "Время - очередная иллюзия, чьим рабом я не желаю быть.");
        assertEquals("Некорректный формат времени. Введите время в виде 00:00 (часы:минуты)",
                wrongTimeMessageResponse.getText());

        BotMessage timeMessageResponse = messageHandler.handle(chatId, "05:00");
        assertEquals("Напоминание создано. Буду присылать прогноз погоды в 05:00",
                timeMessageResponse.getText());
        verify(reminderService).addReminder(chatId, "Екатеринбург", "05:00");
    }

    /**
     * Проверяет полную команду удаления напоминания.<br>
     * Проверки:
     * <ul>
     *     <li>если существует напоминание с такой позицией, то ответное сообщение должно содержать подтверждение
     *     удаления, и при этом напоминание должно быть удалено</li>
     *     <li>если не существует напоминания с такой позицией, то ответное сообщение должно содержать
     *     предупреждение о том, что нет напоминания с такой позицией</li>
     *     <li>если позиция не число, то ответное сообщение должно содержать просьбу ввести число</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест на полную команду удаления напоминания")
    void testFullDeleteSubscriptionCommand() {
        long chatId = 1L;

        BotMessage beforeDeletionMessageResponse = messageHandler.handle(chatId, "/del_subscription 1");
        assertEquals("Напоминание удалено. Больше не буду присылать прогноз погоды.",
                beforeDeletionMessageResponse.getText());
        verify(reminderService).deleteReminderByRelativePosition(chatId, 1);

        doThrow(IllegalArgumentException.class).when(reminderService)
                .deleteReminderByRelativePosition(chatId, 1);

        BotMessage afterDeletionMessageResponse = messageHandler.handle(chatId, "/del_subscription 1");
        assertEquals("Нет напоминания с таким номером.",
                afterDeletionMessageResponse.getText());
        verify(reminderService, times(2)).deleteReminderByRelativePosition(chatId, 1);

        BotMessage notANumberPositionMessageResponse = messageHandler.handle(chatId, "/del_subscription abc");
        assertEquals("Некорректный формат номера напоминания. Используйте только числа при вводе."
                , notANumberPositionMessageResponse.getText());
    }

    /**
     * Проверяет неполную команду удаления напоминания.<br>
     * Проверки:
     * <ul>
     *     <li>если пользователь прислал корректный номер, то бот должен удалить напоминание под этим номером</li>
     *     <li>если пользователь прислал номер несуществующего напоминания, бот должен уведомить что нет напоминания
     *     с таким номером</li>
     *     <li>если пользователь прислал не число, то бот должен попросить ввести номер повторно</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест на неполную команду удаления напоминания")
    void testNotFullDeleteSubscriptionCommand() {
        long chatId = 1L;
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        BotMessage deleteSubscriptionMessageResponse = messageHandler.handle(chatId, "/del_subscription");
        assertEquals("Введите номер напоминания, которое надо удалить",
                deleteSubscriptionMessageResponse.getText());
        assertEquals(1, deleteSubscriptionMessageResponse.getButtons().size());
        assertEquals("Отмена", deleteSubscriptionMessageResponse.getButtons().get(0).getText());
        assertEquals("/cancel", deleteSubscriptionMessageResponse.getButtons().get(0).getCallback());
        verify(reminderService, never()).deleteReminderByRelativePosition(eq(chatId), anyInt());

        BotMessage correctPositionMessageResponse = messageHandler.handle(chatId, "1");
        assertEquals("Напоминание удалено. Больше не буду присылать прогноз погоды.",
                correctPositionMessageResponse.getText());
        verify(reminderService).deleteReminderByRelativePosition(chatId, 1);

        doThrow(IllegalArgumentException.class).when(reminderService)
                .deleteReminderByRelativePosition(chatId, 1000);

        messageHandler.handle(chatId, "/del_subscription");
        BotMessage notExistentPositionMessageResponse = messageHandler.handle(chatId, "1000");
        assertEquals("Нет напоминания с таким номером.",
                notExistentPositionMessageResponse.getText());
        verify(reminderService).deleteReminderByRelativePosition(chatId, 1000);

        messageHandler.handle(chatId, "/del_subscription");
        BotMessage notANumberPositionMessageResponse = messageHandler.handle(chatId, "abc");
        assertEquals("Некорректный формат номера напоминания. Используйте только числа при вводе.",
                notANumberPositionMessageResponse.getText());
    }
}