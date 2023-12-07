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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.urfu.weatherforecastbot.database.ChatStateRepository;
import ru.urfu.weatherforecastbot.model.*;
import ru.urfu.weatherforecastbot.service.ReminderService;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.ReminderFormatterImpl;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Тесты обработчика сообщений
 */
@ExtendWith(MockitoExtension.class)
class MessageHandlerImplTest {

    /**
     * Форматировщик прогноза погоды в удобочитаемый вид
     */
    @Mock
    private WeatherForecastFormatterImpl forecastFormatter;
    /**
     * Сервис для получения прогнозов погоды
     */
    @Mock
    private WeatherForecastService weatherService;
    /**
     * Репозиторий состояний чатов
     */
    @Mock
    private ChatStateRepository chatStateRepository;
    /**
     * Обработчик сообщений
     */
    private MessageHandler messageHandler;
    /**
     * Сервис управления напоминаниями
     */
    private ReminderService reminderService;
    /**
     * Форматировщик напоминаний
     */
    @Mock
    private ReminderFormatterImpl reminderFormatter;

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
        reminderService = Mockito.mock();
        messageHandler =
                new MessageHandlerImpl(weatherService, forecastFormatter, chatStateRepository, reminderService,
                        reminderFormatter);
    }

    @Test
    @DisplayName("Если пользователь прислал не текст, то ответное сообщение должно содержать предупреждение о том, " +
            "что бот понимает только текст")
    void whenUserSendsNonText_thenReturnUnderstandOnlyText() {
        userMessage.setPhoto(List.of());

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Извините, я понимаю только текст.", responseMessage.getText());
    }

    @Test
    @DisplayName("Бот должен отвечать именно тому пользователю, от которого пришло сообщение")
    void whenUserSendsMessage_thenSendMessageToThatUser() {
        ChatState chatState = new ChatState();
        chatState.setChatId(userMessage.getChatId());
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(userMessage.getChatId())).thenReturn(Optional.of(chatState));
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
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
        when(forecastFormatter.formatTodayForecast(todayForecast))
                .thenReturn("Прогноз погоды на сегодня (Екатеринбург): ...");
        userMessage.setText("/info Екатеринбург");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Прогноз погоды на сегодня (Екатеринбург): ...", responseMessage.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на сегодня без указания места бот запросит название места, после чего " +
            "отправит прогноз, при этом временной период не будет запрошен")
    void givenNoPlaceName_whenTodayForecast_thenAskOnlyPlace() {
        long chatId = 1L;
        Chat chat = new Chat();
        chat.setId(chatId);
        Message forecastTodayMessage = new Message();
        forecastTodayMessage.setChat(chat);
        forecastTodayMessage.setText("/info");
        LocalDateTime today = LocalDateTime.now();
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
        when(forecastFormatter.formatTodayForecast(todayForecast))
                .thenReturn("Прогноз погоды на сегодня (Екатеринбург): ...");
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        SendMessage forecastTodayMessageResponse = messageHandler.handle(forecastTodayMessage);
        assertEquals("Введите название места", forecastTodayMessageResponse.getText());
        List<InlineKeyboardButton> forecastWeekResponseButtons = getMessageButtons(forecastTodayMessageResponse);
        assertEquals(1, forecastWeekResponseButtons.size());
        assertTrue(forecastWeekResponseButtons.stream().anyMatch(button -> button.getText().equals("Отмена")));

        Message placeNameMessage = new Message();
        placeNameMessage.setChat(chat);
        placeNameMessage.setText("Екатеринбург");

        SendMessage placeNameMessageResponse = messageHandler.handle(placeNameMessage);
        assertEquals("Прогноз погоды на сегодня (Екатеринбург): ...", placeNameMessageResponse.getText());
    }

    @Test
    @DisplayName("Если не удается найти указанное место, то ответное сообщение должно содержать " +
            "предупреждение о том, что место не найдено")
    void givenNotFoundPlace_whenTodayForecast_thenReturnNotFound() {
        userMessage.setText("/info там_где_нас_нет");
        when(weatherService.getForecast("там_где_нас_нет", 1)).thenReturn(null);

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Извините, данное место не найдено.", responseMessage.getText());
    }

    @Test
    @DisplayName("При вводе неизвестной команды ответное сообщение должно содержать предупреждение о том, что " +
            "бот не знает такой команды")
    void givenUnknownCommand_thenReturnUnknownCommand() {
        ChatState chatState = new ChatState();
        chatState.setChatId(userMessage.getChatId());
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(userMessage.getChatId())).thenReturn(Optional.of(chatState));
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
        when(forecastFormatter.formatTodayForecast(marsTodayForecast))
                .thenReturn("Прогноз погоды на сегодня (Марс): ...");
        when(forecastFormatter.formatTodayForecast(nizhnyNovgorodTodayForecast))
                .thenReturn("Прогноз погоды на сегодня (Нижний Новгород): ...");
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

        SendMessage replyToMarsDweller = messageHandler.handle(marsDwellerMessage);
        SendMessage replyToInstructionsBookworm = messageHandler.handle(instructionsBookwormMessage);
        SendMessage replyToTypicalUser = messageHandler.handle(typicalUserMessage);

        assertEquals(marsDwellerChatId, Long.parseLong(replyToMarsDweller.getChatId()));
        assertEquals("Прогноз погоды на сегодня (Марс): ...", replyToMarsDweller.getText());
        assertEquals(instructionsBookwormChatId, Long.parseLong(replyToInstructionsBookworm.getChatId()));
        assertEquals("Извините, я не знаю такой команды.", replyToInstructionsBookworm.getText());
        assertEquals(typicalUserChatId, Long.parseLong(replyToTypicalUser.getChatId()));
        // проверяем, что корректно работает с названиями, содержащие пробелы
        assertEquals("Прогноз погоды на сегодня (Нижний Новгород): ...", replyToTypicalUser.getText());
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
        when(forecastFormatter.formatWeekForecast(weekForecast))
                .thenReturn("Прогноз погоды на неделю вперед (Екатеринбург): ...");
        userMessage.setText("/info_week Екатеринбург");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Прогноз погоды на неделю вперед (Екатеринбург): ...", responseMessage.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на неделю вперед для ненайденного города " +
            "должно возвращаться сообщение об ошибке")
    void givenNonExistentPlace_whenWeekForecast_thenErrorMessage() {
        userMessage.setText("/info_week там_где_нас_нет");
        when(weatherService.getForecast("там_где_нас_нет", 7)).thenReturn(null);

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Извините, данное место не найдено.", responseMessage.getText());
    }

    @Test
    @DisplayName("При запросе прогноза погоды на неделю вперед без указания города бот запросит название места, после " +
            "чего отправит прогноз погоды, при этом временной период не будет запрошен")
    void givenNoPlaceName_whenWeekForecast_thenAskOnlyPlace() {
        long chatId = 1L;
        Chat chat = new Chat();
        chat.setId(chatId);
        Message forecastWeekMessage = new Message();
        forecastWeekMessage.setChat(chat);
        forecastWeekMessage.setText("/info_week");
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
        when(forecastFormatter.formatWeekForecast(weekForecast))
                .thenReturn("Прогноз погоды на неделю вперед (Екатеринбург): ...");
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        SendMessage forecastWeekMessageResponse = messageHandler.handle(forecastWeekMessage);
        assertEquals("Введите название места", forecastWeekMessageResponse.getText());
        List<InlineKeyboardButton> forecastWeekResponseButtons = getMessageButtons(forecastWeekMessageResponse);
        assertEquals(1, forecastWeekResponseButtons.size());
        assertTrue(forecastWeekResponseButtons.stream().anyMatch(button -> button.getText().equals("Отмена")));

        Message placeNameMessage = new Message();
        placeNameMessage.setChat(chat);
        placeNameMessage.setText("Екатеринбург");

        SendMessage placeNameMessageResponse = messageHandler.handle(placeNameMessage);
        assertEquals("Прогноз погоды на неделю вперед (Екатеринбург): ...", placeNameMessageResponse.getText());
    }

    @Test
    @DisplayName("При вводе команды \"/start\" пользователю должно отобразиться приветствие")
    void givenStartCommand_thenReturnHelloMessage() {
        ChatState chatState = new ChatState();
        chatState.setChatId(userMessage.getChatId());
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(userMessage.getChatId())).thenReturn(Optional.of(chatState));
        userMessage.setText("/start");

        SendMessage responseMessage = messageHandler.handle(userMessage);
        List<InlineKeyboardButton> responseMessageButtons = getMessageButtons(responseMessage);

        assertEquals("""
                        Здравствуйте! Я бот для просмотра прогноза погоды. Доступны следующие команды:
                        /start - запустить бота
                        /help - меню помощи
                        /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                        /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
                        /subscribe <название населенного пункта> <время по Гринвичу> - создать напоминание прогноза погоды
                        /show_subscriptions - показать список напоминаний
                        /edit_subscription <номер напоминания> <новое название населенного пункта> <новое время по Гринвичу> - изменить напоминание прогноза погоды
                        /del_subscription <номер напоминания> - удалить напоминание с указанным номером
                        """,
                responseMessage.getText());
        assertEquals(3, responseMessageButtons.size());
        assertTrue(responseMessageButtons.stream().anyMatch(button -> button.getText().equals("Узнать прогноз")));
        assertTrue(responseMessageButtons.stream().anyMatch(button -> button.getText().equals("Помощь")));
        assertTrue(responseMessageButtons.stream().anyMatch(button -> button.getText().equals("Отмена")));
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
                        /subscribe <название населенного пункта> <время по Гринвичу> - создать напоминание прогноза погоды
                        /show_subscriptions - показать список напоминаний
                        /edit_subscription <номер напоминания> <новое название населенного пункта> <новое время по Гринвичу> - изменить напоминание прогноза погоды
                        /del_subscription <номер напоминания> - удалить напоминание с указанным номером
                        """,
                responseMessage.getText());
    }

    @Test
    @DisplayName("Если пользователь запрашивает прогноз без указания места и времени, то бот должен последовательно " +
            "уточнить все детали, после чего прислать соответствующий прогноз погоды")
    void whenForecast_thenAskDetails() {
        long chatId = 1L;
        Chat chat = new Chat();
        chat.setId(chatId);
        Message forecastMessage = new Message();
        forecastMessage.setChat(chat);
        forecastMessage.setText("Узнать прогноз");
        LocalDateTime today = LocalDateTime.now();
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
        when(forecastFormatter.formatTodayForecast(todayForecast))
                .thenReturn("Прогноз погоды на сегодня: ...");
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        SendMessage forecastMessageResponse = messageHandler.handle(forecastMessage);
        assertEquals("Введите название места", forecastMessageResponse.getText());

        Message placeNameMessage = new Message();
        placeNameMessage.setChat(chat);
        placeNameMessage.setText("Екатеринбург");

        SendMessage placeNameMessageResponse = messageHandler.handle(placeNameMessage);
        List<InlineKeyboardButton> placeNameMessageButtons = getMessageButtons(placeNameMessageResponse);
        assertEquals("Выберите временной период для просмотра (сегодня, завтра, неделя)",
                placeNameMessageResponse.getText());
        assertEquals(4, placeNameMessageButtons.size());
        assertTrue(placeNameMessageButtons.stream().anyMatch(button -> button.getText().equals("Сегодня")));
        assertTrue(placeNameMessageButtons.stream().anyMatch(button -> button.getText().equals("Завтра")));
        assertTrue(placeNameMessageButtons.stream().anyMatch(button -> button.getText().equals("Неделя")));
        assertTrue(placeNameMessageButtons.stream().anyMatch(button -> button.getText().equals("Отмена")));

        Message timePeriodMessage = new Message();
        timePeriodMessage.setChat(chat);
        timePeriodMessage.setText("сегодня");

        SendMessage timePeriodMessageResponse = messageHandler.handle(timePeriodMessage);
        assertEquals("Прогноз погоды на сегодня: ...", timePeriodMessageResponse.getText());
    }

    @Test
    @DisplayName("Если пользователь во время запроса погоды присылает некорректный временной период, то ответное " +
            "сообщение должно содержать просьбу ввести временной период повторно")
    void givenUserSendsWrongTimePeriod_whenForecast_thenAskTimePeriodAgain() {
        long chatId = 1L;
        Chat chat = new Chat();
        chat.setId(chatId);
        Message forecastMessage = new Message();
        forecastMessage.setChat(chat);
        forecastMessage.setText("Узнать прогноз");
        Message placeNameMessage = new Message();
        placeNameMessage.setChat(chat);
        placeNameMessage.setText("Екатеринбург");
        Message wrongTimePeriodMessage = new Message();
        wrongTimePeriodMessage.setChat(chat);
        wrongTimePeriodMessage.setText("привет");
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        messageHandler.handle(forecastMessage);
        messageHandler.handle(placeNameMessage);
        SendMessage wrongTimePeriodMessageResponse = messageHandler.handle(wrongTimePeriodMessage);

        assertEquals("Введите корректный временной период. Допустимые значения: сегодня, завтра, неделя",
                wrongTimePeriodMessageResponse.getText());
        List<InlineKeyboardButton> responseMessageButtons = getMessageButtons(wrongTimePeriodMessageResponse);
        assertEquals(4, responseMessageButtons.size());
        assertTrue(responseMessageButtons.stream().anyMatch(button -> button.getText().equals("Сегодня")));
        assertTrue(responseMessageButtons.stream().anyMatch(button -> button.getText().equals("Завтра")));
        assertTrue(responseMessageButtons.stream().anyMatch(button -> button.getText().equals("Неделя")));
        assertTrue(responseMessageButtons.stream().anyMatch(button -> button.getText().equals("Отмена")));
    }

    @Test
    @DisplayName("Если пользователь отменяет действие, то ответное сообщение должно содержать уведомление о  " +
            "возврате в меню")
    void whenCancel_thenReturnToMenu() {
        long chatId = 1L;
        Chat chat = new Chat();
        chat.setId(chatId);
        Message forecastMessage = new Message();
        forecastMessage.setChat(chat);
        forecastMessage.setText("Узнать прогноз");
        Message cancelMessage = new Message();
        cancelMessage.setChat(chat);
        cancelMessage.setText("/cancel");
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));

        messageHandler.handle(forecastMessage);
        SendMessage responseMessage = messageHandler.handle(cancelMessage);

        assertEquals("Вы вернулись в основное меню", responseMessage.getText());
        List<InlineKeyboardButton> responseMessageButtons = getMessageButtons(responseMessage);
        assertEquals(3, responseMessageButtons.size());
        assertTrue(responseMessageButtons.stream().anyMatch(button -> button.getText().equals("Узнать прогноз")));
        assertTrue(responseMessageButtons.stream().anyMatch(button -> button.getText().equals("Помощь")));
        assertTrue(responseMessageButtons.stream().anyMatch(button -> button.getText().equals("Отмена")));
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
        Chat chat = new Chat();
        chat.setId(chatId);
        Message correctTimeMessage = new Message();
        correctTimeMessage.setText("/subscribe Екатеринбург 05:00");
        correctTimeMessage.setChat(chat);

        SendMessage correctTimeMessageResponse = messageHandler.handle(correctTimeMessage);
        assertEquals("Напоминание создано. Буду присылать прогноз погоды в 05:00",
                correctTimeMessageResponse.getText());
        verify(reminderService).addReminder(chatId, "Екатеринбург", "05:00");

        Message wrongTimeMessage = new Message();
        wrongTimeMessage.setText("/subscribe Екатеринбург abc");
        wrongTimeMessage.setChat(chat);
        doThrow(IllegalArgumentException.class).when(reminderService)
                .addReminder(chatId, "Екатеринбург", "abc");

        SendMessage wrongTimeMessageResponse = messageHandler.handle(wrongTimeMessage);
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
        Chat chat = new Chat();
        chat.setId(chatId);
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));
        Message subscribeMessage = new Message();
        subscribeMessage.setChat(chat);
        subscribeMessage.setText("/subscribe");

        SendMessage subscribeMessageResponse = messageHandler.handle(subscribeMessage);
        assertEquals("Введите название места, для которого будут присылаться напоминания",
                subscribeMessageResponse.getText());
        List<InlineKeyboardButton> subscribeMessageButtons = getMessageButtons(subscribeMessageResponse);
        assertEquals(1, subscribeMessageButtons.size());
        assertTrue(subscribeMessageButtons.stream().anyMatch(button -> button.getText().equals("Отмена")));

        Message placeNameMessage = new Message();
        placeNameMessage.setText("Екатеринбург");
        placeNameMessage.setChat(chat);

        SendMessage placeNameMessageResponse = messageHandler.handle(placeNameMessage);
        assertEquals("Введите время (в UTC), когда должно присылаться напоминание прогноза (пример: 08:00)",
                placeNameMessageResponse.getText());
        List<InlineKeyboardButton> placeNameMessageButtons = getMessageButtons(placeNameMessageResponse);
        assertEquals(1, placeNameMessageButtons.size());
        assertTrue(placeNameMessageButtons.stream().anyMatch(button -> button.getText().equals("Отмена")));

        // проверка на некорретный формат времени
        Message wrongTimeMessage = new Message();
        wrongTimeMessage.setText("Время - очередная иллюзия, чьим рабом я не желаю быть.");
        wrongTimeMessage.setChat(chat);
        doThrow(IllegalArgumentException.class).when(reminderService)
                .addReminder(chatId, "Екатеринбург", wrongTimeMessage.getText());

        SendMessage wrongTimeMessageResponse = messageHandler.handle(wrongTimeMessage);
        assertEquals("Некорректный формат времени. Введите время в виде 00:00 (часы:минуты)",
                wrongTimeMessageResponse.getText());

        Message timeMessage = new Message();
        timeMessage.setText("05:00");
        timeMessage.setChat(chat);

        SendMessage timeMessageResponse = messageHandler.handle(timeMessage);
        assertEquals("Напоминание создано. Буду присылать прогноз погоды в 05:00",
                timeMessageResponse.getText());
        verify(reminderService).addReminder(chatId, "Екатеринбург", "05:00");
    }

    /**
     * Проверяет корректное выполнение команды и форматирование списка напоминаний.<br>
     * <br>
     * Проверки:
     * <ul>
     *     <li>Если у пользователя есть созданные напоминания,
     *     то бот должен вернуть отформатированный список напоминаний.</li>
     *     <li>Если у пользователя нет созданных напоминаний,
     *     то бот должен вернуть сообщение о пустом списке.</li>
     * </ul>
     * <br>
     */
    @Test
    @DisplayName("Тест на команду просмотра списка напоминаний")
    void testShowSubscriptionsCommand() {
        String placeName = "Екатеринбург";
        Reminder firstReminder = new Reminder();
        firstReminder.setId(1L);
        firstReminder.setChatId(userMessage.getChatId());
        firstReminder.setPlaceName(placeName);
        firstReminder.setTime(LocalTime.of(5, 0));
        Reminder secondReminder = new Reminder();
        secondReminder.setId(2L);
        secondReminder.setChatId(userMessage.getChatId());
        secondReminder.setPlaceName(placeName);
        secondReminder.setTime(LocalTime.of(17, 0));
        List<Reminder> reminders = List.of(firstReminder, secondReminder);
        when(reminderService.findAllForChatId(userMessage.getChatId())).thenReturn(reminders);
        when(reminderFormatter.formatReminders(reminders)).thenReturn("""
                1) Екатеринбург, 05:00
                2) Екатеринбург, 17:00
                """);
        userMessage.setText("/show_subscriptions");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("""
                1) Екатеринбург, 05:00
                2) Екатеринбург, 17:00
                """, responseMessage.getText());
    }

    /**
     * Проверяет корректное выполнение команды и изменение соответствующего напоминания.<br>
     * <br>
     * Проверки:
     * <ul>
     *     <li>Если пользователь вводит корректные данные для редактирования напоминания,
     *     то бот должен подтвердить изменение.</li>
     *     <li>Если пользователь вводит некорректные данные,
     *     то бот должен вернуть соответствующее сообщение об ошибке.</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест на команду редактирования напоминания")
    void testEditSubscriptionCommand() {
        userMessage.setText("/edit_subscription 1 Москва 10:00");

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Напоминание изменено. Буду присылать прогноз погоды в 10:00",
                responseMessage.getText());
        verify(reminderService)
                .editReminderByRelativePosition(
                        userMessage.getChatId(),
                        1,
                        "Москва",
                        "10:00");
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
        Chat chat = new Chat();
        chat.setId(chatId);
        Message beforeDeletionMessage = new Message();
        beforeDeletionMessage.setChat(chat);
        beforeDeletionMessage.setText("/del_subscription 1");

        SendMessage beforeDeletionMessageResponse = messageHandler.handle(beforeDeletionMessage);
        assertEquals("Напоминание удалено. Больше не буду присылать прогноз погоды.",
                beforeDeletionMessageResponse.getText());
        verify(reminderService).deleteReminderByRelativePosition(chatId, 1);

        Message afterDeletionMessage = new Message();
        afterDeletionMessage.setChat(chat);
        afterDeletionMessage.setText("/del_subscription 1");
        doThrow(IllegalArgumentException.class).when(reminderService)
                .deleteReminderByRelativePosition(chatId, 1);

        SendMessage afterDeletionMessageResponse = messageHandler.handle(beforeDeletionMessage);
        assertEquals("Нет напоминания с таким номером.",
                afterDeletionMessageResponse.getText());
        verify(reminderService, times(2)).deleteReminderByRelativePosition(chatId, 1);

        Message notANumberPositionMessage = new Message();
        notANumberPositionMessage.setText("/del_subscription abc");
        notANumberPositionMessage.setChat(chat);

        SendMessage notANumberPositionMessageResponse = messageHandler.handle(notANumberPositionMessage);
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
        Chat chat = new Chat();
        chat.setId(chatId);
        ChatState chatState = new ChatState();
        chatState.setChatId(chatId);
        chatState.setBotState(BotState.INITIAL);
        when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));
        Message deleteSubscriptionMessage = new Message();
        deleteSubscriptionMessage.setChat(chat);
        deleteSubscriptionMessage.setText("/del_subscription");

        SendMessage deleteSubscriptionMessageResponse = messageHandler.handle(deleteSubscriptionMessage);
        assertEquals("Введите номер напоминания, которое надо удалить",
                deleteSubscriptionMessageResponse.getText());
        List<InlineKeyboardButton> deleteSubscriptionMessageButtons =
                getMessageButtons(deleteSubscriptionMessageResponse);
        assertEquals(1, deleteSubscriptionMessageButtons.size());
        assertTrue(deleteSubscriptionMessageButtons.stream().anyMatch(button -> button.getText().equals("Отмена")));
        verify(reminderService, never()).deleteReminderByRelativePosition(eq(chatId), anyInt());

        Message correctPositionMessage = new Message();
        correctPositionMessage.setText("1");
        correctPositionMessage.setChat(chat);

        SendMessage correctPositionMessageResponse = messageHandler.handle(correctPositionMessage);
        assertEquals("Напоминание удалено. Больше не буду присылать прогноз погоды.",
                correctPositionMessageResponse.getText());
        verify(reminderService).deleteReminderByRelativePosition(chatId, 1);

        Message notExistentPositionMessage = new Message();
        notExistentPositionMessage.setText("1000");
        notExistentPositionMessage.setChat(chat);
        doThrow(IllegalArgumentException.class).when(reminderService)
                .deleteReminderByRelativePosition(chatId, 1000);

        messageHandler.handle(deleteSubscriptionMessage);
        SendMessage notExistentPositionMessageResponse = messageHandler.handle(notExistentPositionMessage);
        assertEquals("Нет напоминания с таким номером.",
                notExistentPositionMessageResponse.getText());
        verify(reminderService).deleteReminderByRelativePosition(chatId, 1000);

        messageHandler.handle(deleteSubscriptionMessage);
        Message notANumberPositionMessage = new Message();
        notANumberPositionMessage.setText("abc");
        notANumberPositionMessage.setChat(chat);

        SendMessage notANumberPositionMessageResponse = messageHandler.handle(notANumberPositionMessage);
        assertEquals("Некорректный формат номера напоминания. Используйте только числа при вводе.",
                notANumberPositionMessageResponse.getText());
    }

    /**
     * Возвращает список кнопок, прикрепленных к сообщению
     *
     * @param message сообщение
     * @return список кнопок, прикрепленных к сообщению
     */
    private List<InlineKeyboardButton> getMessageButtons(SendMessage message) {
        InlineKeyboardMarkup messageMarkup = (InlineKeyboardMarkup) message.getReplyMarkup();
        return messageMarkup.getKeyboard().stream()
                .flatMap(List::stream)
                .toList();
    }
}