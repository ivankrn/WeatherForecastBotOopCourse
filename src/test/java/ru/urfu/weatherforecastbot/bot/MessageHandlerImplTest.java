package ru.urfu.weatherforecastbot.bot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.urfu.weatherforecastbot.database.ChatStateRepository;
import ru.urfu.weatherforecastbot.model.BotState;
import ru.urfu.weatherforecastbot.model.ChatState;
import ru.urfu.weatherforecastbot.model.Place;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.ForecastTimePeriod;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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
     * Репозиторий состояний чатов
     */
    private final ChatStateRepository chatStateRepository;
    /**
     * Обработчик сообщений
     */
    private final MessageHandler messageHandler;

    public MessageHandlerImplTest(@Mock WeatherForecastService weatherService,
                                  @Mock ChatStateRepository chatStateRepository) {
        this.weatherService = weatherService;
        this.chatStateRepository = chatStateRepository;
        messageHandler = new MessageHandlerImpl(weatherService, forecastFormatter, chatStateRepository);
    }

    @Test
    @DisplayName("При запросе прогноза погоды на сегодня в определенном месте ответное сообщение должно содержать прогноз " +
            "погоды на сегодня в том же самом определенном месте")
    void givenPlace_whenTodayForecast_thenReturnTodayForecastForThatPlace() {
        LocalDateTime today = LocalDateTime.now();
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
        String expected = forecastFormatter.formatForecasts(ForecastTimePeriod.TODAY, todayForecast);

        BotMessage responseMessage = messageHandler.handle(1L, "/info Екатеринбург");

        assertEquals(expected, responseMessage.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на сегодня без указания места бот запросит название места, после чего " +
            "отправит прогноз, при этом временной период не будет запрошен")
    void givenNoPlaceName_whenTodayForecast_thenAskOnlyPlace() {
        long chatId = 1L;
        LocalDateTime today = LocalDateTime.now();
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
        String expected = forecastFormatter.formatForecasts(ForecastTimePeriod.TODAY, todayForecast);
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        BotMessage forecastTodayMessageResponse = messageHandler.handle(chatId, "/info");
        assertEquals("Введите название места", forecastTodayMessageResponse.getText());
        assertEquals(BotState.WAITING_FOR_PLACE_NAME, chatState.getBotState());
        List<Button> forecastTodayMessageButtons = forecastTodayMessageResponse.getButtons();
        assertEquals(1, forecastTodayMessageButtons.size());
        assertEquals("Отмена", forecastTodayMessageButtons.get(0).getText());
        assertEquals("/cancel", forecastTodayMessageButtons.get(0).getCallback());

        BotMessage placeNameMessageResponse = messageHandler.handle(chatId, "Екатеринбург");
        assertEquals(expected, placeNameMessageResponse.getText());
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
        LocalDateTime today = LocalDateTime.now();
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
        String expectedMarsForecast = forecastFormatter.formatForecasts(ForecastTimePeriod.TODAY, marsTodayForecast);
        String expectedNizhnyNovgorodForecast =
                forecastFormatter.formatForecasts(ForecastTimePeriod.TODAY, nizhnyNovgorodTodayForecast);
        ChatState marsDwellerChatState = new ChatState();
        marsDwellerChatState.setChatId(marsDwellerChatId);
        marsDwellerChatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(marsDwellerChatId))
                .thenReturn(Optional.of(marsDwellerChatState));
        ChatState instructionsBookwormChatState = new ChatState();
        instructionsBookwormChatState.setChatId(instructionsBookwormChatId);
        instructionsBookwormChatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(instructionsBookwormChatId))
                .thenReturn(Optional.of(instructionsBookwormChatState));
        ChatState typicalUserChatState = new ChatState();
        typicalUserChatState.setChatId(typicalUserChatId);
        typicalUserChatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(typicalUserChatId))
                .thenReturn(Optional.of(typicalUserChatState));

        BotMessage replyToMarsDweller = messageHandler.handle(marsDwellerChatId, "/info Марс");
        BotMessage replyToInstructionsBookworm =
                messageHandler.handle(instructionsBookwormChatId, "/some_unknown_command Москва");
        BotMessage replyToTypicalUser = messageHandler.handle(typicalUserChatId, "/info Нижний Новгород");

        assertEquals(expectedMarsForecast, replyToMarsDweller.getText());
        assertEquals("Извините, я не знаю такой команды.", replyToInstructionsBookworm.getText());
        // проверяем, что корректно работает с названиями, содержащие пробелы
        assertEquals(expectedNizhnyNovgorodForecast, replyToTypicalUser.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на неделю вперед в определенном месте ответное " +
            "сообщение должно содержать прогноз погоды на неделю вперед в том же самом определенном месте")
    void givenPlace_whenWeekForecast_thenReturnFormattedWeekForecast() {
        LocalDateTime now = LocalDateTime.now();
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
        String expected = forecastFormatter.formatForecasts(ForecastTimePeriod.WEEK, weekForecast);

        BotMessage responseMessage = messageHandler.handle(1L, "/info_week Екатеринбург");

        assertEquals(expected, responseMessage.getText());
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
        LocalDateTime now = LocalDateTime.now();
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
        String expected = forecastFormatter.formatForecasts(ForecastTimePeriod.WEEK, weekForecast);
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        BotMessage forecastWeekMessageResponse = messageHandler.handle(chatId, "/info_week");
        assertEquals("Введите название места", forecastWeekMessageResponse.getText());
        assertEquals(BotState.WAITING_FOR_PLACE_NAME, chatState.getBotState());
        List<Button> forecastWeekMessageButtons = forecastWeekMessageResponse.getButtons();
        assertEquals(1, forecastWeekMessageButtons.size());
        assertEquals("Отмена", forecastWeekMessageButtons.get(0).getText());
        assertEquals("/cancel", forecastWeekMessageButtons.get(0).getCallback());

        BotMessage placeNameMessageResponse = messageHandler.handle(chatId, "Екатеринбург");
        assertEquals(expected, placeNameMessageResponse.getText());
        assertEquals(BotState.INITIAL, chatState.getBotState());
    }

    @Test
    @DisplayName("При вводе команды \"/start\" пользователю должно отобразиться приветствие")
    void givenStartCommand_thenReturnHelloMessage() {
        long chatId = 1L;
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
                        """,
                responseMessage.getText());
        assertEquals(3, responseButtons.size());
        assertEquals("Узнать прогноз", responseButtons.get(0).getText());
        assertEquals("Узнать прогноз", responseButtons.get(0).getCallback());
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
                        """,
                responseMessage.getText());
    }

    @Test
    @DisplayName("Если пользователь запрашивает прогноз без указания места и времени, то бот должен последовательно " +
            "уточнить все детали, после чего прислать соответствующий прогноз погоды")
    void whenForecast_thenAskDetails() {
        long chatId = 1L;
        LocalDateTime today = LocalDateTime.now();
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
        String expected = forecastFormatter.formatForecasts(ForecastTimePeriod.TODAY, todayForecast);
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        BotMessage forecastMessageResponse = messageHandler.handle(chatId, "Узнать прогноз");
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
        assertEquals(expected, timePeriodMessageResponse.getText());
        assertEquals(BotState.INITIAL, chatState.getBotState());
    }

    @Test
    @DisplayName("Если пользователь во время запроса погоды присылает некорректный временной период, то ответное " +
            "сообщение должно содержать просьбу ввести временной период повторно")
    void givenUserSendsWrongTimePeriod_whenForecast_thenAskTimePeriodAgain() {
        long chatId = 1L;
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        messageHandler.handle(chatId, "Узнать прогноз");
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
        assertEquals("Узнать прогноз", responseButtons.get(0).getCallback());
        assertEquals("Помощь", responseButtons.get(1).getText());
        assertEquals("/help", responseButtons.get(1).getCallback());
        assertEquals("Отмена", responseButtons.get(2).getText());
        assertEquals("/cancel", responseButtons.get(2).getCallback());
    }

}