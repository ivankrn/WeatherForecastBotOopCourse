package ru.urfu.weatherforecastbot.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.urfu.weatherforecastbot.database.ChatStateRepository;
import ru.urfu.weatherforecastbot.model.BotState;
import ru.urfu.weatherforecastbot.model.ChatState;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;

import java.time.LocalDateTime;
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
    private WeatherForecastFormatter forecastFormatter;
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
    @DisplayName("Если пользователь прислал не текст, то ответное сообщение должно содержать предупреждение о том, " +
            "что бот понимает только текст")
    void whenUserSendsNonText_thenReturnUnderstandOnlyText() {
        userMessage.setPhoto(List.of());

        SendMessage responseMessage = messageHandler.handle(userMessage);

        assertEquals("Извините, я понимаю только текст.", responseMessage.getText());
    }

    @Nested
    @DisplayName("Тесты при взаимодействии с ботом в форме команд")
    class CommandHandling {

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
            List<InlineKeyboardButton> responseMessageButtons = getMessageButtons(responseMessage);

            assertEquals("""
                            Здравствуйте! Я бот для просмотра прогноза погоды. Доступны следующие команды:
                            /start - запустить бота
                            /help - меню помощи
                            /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                            /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
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
                            """,
                    responseMessage.getText());
        }
    }

    @Nested
    @DisplayName("Тесты при взаимодействии с ботом в форме диалога")
    class DialogHandling {

        @Test
        @DisplayName("Если пользователь впервые пишет боту, то новое состояние чата должно сохраниться")
        void givenNewUser_whenFirstMessage_thenSaveState() {
            userMessage.setText("/start");

            messageHandler.handle(userMessage);

            verify(chatStateRepository).save(argThat((ChatState chatState) ->
                    chatState.getChatId() == userMessage.getChatId() && chatState.getBotState() == BotState.INITIAL));
        }

        @Test
        @DisplayName("Если пользователь запрашивает меню помощи, то ответное сообщение должно содержать" +
                " меню помощи")
        void whenHelp_thenReturnHelp() {
            ChatState chatState = new ChatState();
            chatState.setChatId(userMessage.getChatId());
            chatState.setBotState(BotState.INITIAL);
            when(chatStateRepository.existsById(userMessage.getChatId())).thenReturn(true);
            when(chatStateRepository.findById(userMessage.getChatId())).thenReturn(Optional.of(chatState));

            userMessage.setText("помощь");

            SendMessage responseMessage = messageHandler.handle(userMessage);
            assertEquals("""
                            Вы зашли в меню помощи. Для вас доступны следующие команды:
                            /start - запустить бота
                            /help - меню помощи
                            /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                            /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
                            """
                    , responseMessage.getText());
        }

        @Test
        @DisplayName("Если пользователь запрашивает прогноз, то бот должен последовательно уточнить все детали, после " +
                "чего прислать соответствующий прогноз погоды")
        void whenForecast_thenAskDetails() {
            long chatId = 1L;
            Chat chat = new Chat();
            chat.setId(chatId);
            Message forecastMessage = new Message();
            forecastMessage.setChat(chat);
            forecastMessage.setText("Узнать прогноз");
            ChatState chatState = new ChatState();
            chatState.setChatId(chatId);
            chatState.setBotState(BotState.INITIAL);
            when(chatStateRepository.existsById(chatId)).thenReturn(true);
            when(chatStateRepository.findById(chatId)).thenReturn(Optional.of(chatState));
            LocalDateTime today = LocalDateTime.now();
            int hours = 24;
            List<WeatherForecast> todayForecast = new ArrayList<>(hours);
            for (int hour = 0; hour < hours; hour++) {
                todayForecast.add(
                        new WeatherForecast(today.withHour(hour), 0, 0));
            }
            when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);
            when(forecastFormatter.formatTodayForecast(todayForecast))
                    .thenReturn("Прогноз погоды на сегодня: ...");

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
            when(chatStateRepository.existsById(chatId)).thenReturn(true);
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
            cancelMessage.setText("Отмена");
            ChatState chatState = new ChatState();
            chatState.setChatId(chatId);
            chatState.setBotState(BotState.INITIAL);
            when(chatStateRepository.existsById(chatId)).thenReturn(true);
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

        @Test
        @DisplayName("При взаимодействии с несколькими пользователями в форме диалога, бот должен отвечать каждому  " +
                "пользователю соответственно")
        void givenSeveralUsers_whenDialog_thenAnswerEveryone() {
            long curiousUserChatId = 1L;
            Chat curiousUserChat = new Chat();
            curiousUserChat.setId(curiousUserChatId);
            Message curiousUserMessage = new Message();
            curiousUserMessage.setChat(curiousUserChat);
            curiousUserMessage.setText("Узнать прогноз");
            ChatState curiousUserChatState = new ChatState();
            curiousUserChatState.setChatId(curiousUserChatId);
            curiousUserChatState.setBotState(BotState.INITIAL);
            when(chatStateRepository.existsById(curiousUserChatId)).thenReturn(true);
            when(chatStateRepository.findById(curiousUserChatId)).thenReturn(Optional.of(curiousUserChatState));
            long manualReaderChatId = 2L;
            Chat manualReaderChat = new Chat();
            manualReaderChat.setId(manualReaderChatId);
            Message manualReaderMessage = new Message();
            manualReaderMessage.setChat(manualReaderChat);
            manualReaderMessage.setText("Помощь");
            ChatState manualReaderChatState = new ChatState();
            manualReaderChatState.setChatId(manualReaderChatId);
            manualReaderChatState.setBotState(BotState.INITIAL);
            when(chatStateRepository.existsById(manualReaderChatId)).thenReturn(true);
            when(chatStateRepository.findById(manualReaderChatId)).thenReturn(Optional.of(manualReaderChatState));

            SendMessage curiousUserMessageResponse = messageHandler.handle(curiousUserMessage);
            SendMessage manualReaderMessageResponse = messageHandler.handle(manualReaderMessage);

            assertEquals(curiousUserChatId, Long.parseLong(curiousUserMessageResponse.getChatId()));
            assertEquals("Введите название места", curiousUserMessageResponse.getText());
            assertEquals(manualReaderChatId, Long.parseLong(manualReaderMessageResponse.getChatId()));
            assertEquals("""
                 Вы зашли в меню помощи. Для вас доступны следующие команды:
                 /start - запустить бота
                 /help - меню помощи
                 /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                 /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд.
                 """, manualReaderMessageResponse.getText());
        }

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