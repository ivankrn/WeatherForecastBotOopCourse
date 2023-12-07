package ru.urfu.weatherforecastbot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.urfu.weatherforecastbot.database.ChatStateRepository;
import ru.urfu.weatherforecastbot.model.BotState;
import ru.urfu.weatherforecastbot.model.ChatState;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.service.ReminderService;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.util.List;

@Component
public class MessageHandlerImpl implements MessageHandler {

    /**
     * Сервис для получения прогнозов погоды
     */
    private final WeatherForecastService weatherService;
    /**
     * Форматировщик прогноза погоды в удобочитаемый вид
     */
    private final WeatherForecastFormatterImpl forecastFormatter;
    /**
     * Репозиторий состояний чатов
     */
    private final ChatStateRepository chatStateRepository;
    /**
     * Сервис для управления напоминаниями
     */
    private final ReminderService reminderService;

    /**
     * Создает экземпляр MessageHandlerImpl, используя в качестве {@link MessageHandlerImpl#forecastFormatter
     * forecastFormatter} {@link WeatherForecastFormatterImpl}
     *
     * @param weatherService сервис для получения прогнозов погоды
     * @param chatStateRepository репозиторий состояний чатов
     * @param reminderService сервис управления напоминаниями
     */
    @Autowired
    public MessageHandlerImpl(WeatherForecastService weatherService, ChatStateRepository chatStateRepository,
                              ReminderService reminderService) {
        this.weatherService = weatherService;
        forecastFormatter = new WeatherForecastFormatterImpl();
        this.chatStateRepository = chatStateRepository;
        this.reminderService = reminderService;
    }

    /**
     * Создает экземпляр MessageHandlerImpl, используя переданные аргументы
     *
     * @param weatherService    сервис для получения прогнозов погоды
     * @param forecastFormatter форматировщик прогноза погоды в удобочитаемый вид
     * @param chatStateRepository репозиторий состояний чатов
     * @param reminderService сервис управления напоминаниями
     */
    public MessageHandlerImpl(WeatherForecastService weatherService,
                              WeatherForecastFormatterImpl forecastFormatter, ChatStateRepository chatStateRepository,
                              ReminderService reminderService) {
        this.weatherService = weatherService;
        this.forecastFormatter = forecastFormatter;
        this.chatStateRepository = chatStateRepository;
        this.reminderService = reminderService;
    }

    @Override
    public SendMessage handle(Message message) {
        long chatId = message.getChatId();
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);
        ChatState chatState = chatStateRepository.findById(chatId).orElseGet(() -> {
            ChatState newChatState = new ChatState();
            newChatState.setChatId(chatId);
            newChatState.setBotState(BotState.INITIAL);
            return chatStateRepository.save(newChatState);
        });
        if (message.hasText()) {
            String messageText = message.getText();
            String[] splittedText = messageText.split(" ");
            String command = splittedText[0];
            switch (command) {
                case BotConstants.COMMAND_START -> {
                    chatState.setBotState(BotState.INITIAL);
                    chatState.setPlaceName(null);
                    chatState.setTime(null);
                    chatStateRepository.save(chatState);
                    responseMessage.setText(BotConstants.START_TEXT);
                    responseMessage.setReplyMarkup(getMainMenuReplyMarkup());
                }
                case BotConstants.COMMAND_HELP -> responseMessage.setText(BotConstants.HELP_TEXT);
                case BotConstants.COMMAND_CANCEL -> {
                    chatState.setBotState(BotState.INITIAL);
                    chatState.setPlaceName(null);
                    chatState.setTime(null);
                    chatStateRepository.save(chatState);
                    responseMessage.setText("Вы вернулись в основное меню");
                    responseMessage.setReplyMarkup(getMainMenuReplyMarkup());
                }
                case BotConstants.COMMAND_FORECAST_TODAY -> {
                    if (splittedText.length < 2) {
                        return handleNonCommand(chatId, command);
                    } else {
                        String place = message.getText().substring(message.getText().indexOf(" ") + 1);
                        responseMessage.setText(handleTodayForecasts(place));
                    }
                }
                case BotConstants.COMMAND_FORECAST_WEEK -> {
                    if (splittedText.length < 2) {
                        return handleNonCommand(chatId, command);
                    } else {
                        String place = message.getText().substring(message.getText().indexOf(" ") + 1);
                        responseMessage.setText(handleWeekForecasts(place));
                    }
                }
                case BotConstants.COMMAND_SUBSCRIBE -> {
                    if (splittedText.length < 3) {
                        return handleNonCommand(chatId, command);
                    } else {
                        String place = splittedText[1];
                        String time = splittedText[2];
                        responseMessage.setText(handleNewSubscription(chatId, place, time));
                    }
                }
                case BotConstants.COMMAND_DEL_SUBSCRIPTION ->  {
                    if (splittedText.length < 2) {
                        return handleNonCommand(chatId, command);
                    } else {
                        String position = splittedText[1];
                        responseMessage.setText(handleDeleteSubscription(chatId, position));
                    }
                }
                default -> {
                    return handleNonCommand(chatId, messageText);
                }
            }
        } else {
            responseMessage.setText(BotConstants.UNDERSTAND_ONLY_TEXT);
        }
        return responseMessage;
    }

    /**
     * Обрабатывает сообщение пользователя, если оно не содержит команду (или содержит неполную), и возвращает ответное
     * сообщение
     *
     * @param chatId ID чата
     * @param text   текст, присланный пользователем
     * @return ответное сообщение
     */
    private SendMessage handleNonCommand(long chatId, String text) {
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);
        ChatState chatState = chatStateRepository.findById(chatId).get();
        BotState currentBotState = chatState.getBotState();
        switch (currentBotState) {
            case INITIAL -> {
                switch (text) {
                    case BotConstants.FORECAST_BUTTON_TEXT,
                            BotConstants.COMMAND_FORECAST_TODAY,
                            BotConstants.COMMAND_FORECAST_WEEK -> {
                        chatState.setBotState(BotState.WAITING_FOR_FORECAST_PLACE_NAME);
                        if (text.equals(BotConstants.COMMAND_FORECAST_TODAY)) {
                            chatState.setTime(BotConstants.TODAY);
                        } else if (text.equals(BotConstants.COMMAND_FORECAST_WEEK)) {
                            chatState.setTime(BotConstants.WEEK);
                        }
                        chatStateRepository.save(chatState);
                        responseMessage.setText("Введите название места");
                        responseMessage.setReplyMarkup(getCancelReplyMarkup());
                    }
                    case BotConstants.COMMAND_SUBSCRIBE -> {
                        chatState.setBotState(BotState.WAITING_FOR_REMINDER_PLACE_NAME);
                        chatStateRepository.save(chatState);
                        responseMessage.setText("Введите название места, для которого будут присылаться напоминания");
                        responseMessage.setReplyMarkup(getCancelReplyMarkup());
                    }
                    case BotConstants.COMMAND_DEL_SUBSCRIPTION -> {
                        chatState.setBotState(BotState.WAITING_FOR_REMINDER_POSITION_TO_DELETE);
                        chatStateRepository.save(chatState);
                        responseMessage.setText("Введите номер напоминания, которое надо удалить");
                        responseMessage.setReplyMarkup(getCancelReplyMarkup());
                    }
                    default -> responseMessage.setText(BotConstants.UNKNOWN_COMMAND);
                }
            }
            case WAITING_FOR_FORECAST_PLACE_NAME -> {
                chatState.setPlaceName(text);
                if (chatState.getTime().isPresent()) {
                    String timePeriod = chatState.getTime().get();
                    if (timePeriod.equals(BotConstants.TODAY)) {
                        responseMessage.setText(handleTodayForecasts(chatState.getPlaceName()));
                    } else if (timePeriod.equals(BotConstants.WEEK)) {
                        responseMessage.setText(handleWeekForecasts(chatState.getPlaceName()));
                    }
                    chatState.setBotState(BotState.INITIAL);
                    chatState.setPlaceName(null);
                    chatState.setTime(null);
                } else {
                    chatState.setBotState(BotState.WAITING_FOR_FORECAST_TIME_PERIOD);
                    responseMessage.setText("Выберите временной период для просмотра (сегодня, завтра, неделя)");
                    responseMessage.setReplyMarkup(getTimePeriodMenuReplyMarkup());
                }
                chatStateRepository.save(chatState);
            }
            case WAITING_FOR_FORECAST_TIME_PERIOD -> {
                if (text.equalsIgnoreCase(BotConstants.TODAY)) {
                    chatState.setBotState(BotState.INITIAL);
                    chatStateRepository.save(chatState);
                    responseMessage.setText(handleTodayForecasts(chatState.getPlaceName()));
                } else if (text.equalsIgnoreCase(BotConstants.TOMORROW)) {
                    chatState.setBotState(BotState.INITIAL);
                    chatStateRepository.save(chatState);
                    responseMessage.setText(handleTomorrowForecasts(chatState.getPlaceName()));
                } else if (text.equalsIgnoreCase(BotConstants.WEEK)) {
                    chatState.setBotState(BotState.INITIAL);
                    chatStateRepository.save(chatState);
                    responseMessage.setText(handleWeekForecasts(chatState.getPlaceName()));
                } else {
                    responseMessage.setText("Введите корректный временной период. " +
                            "Допустимые значения: сегодня, завтра, неделя");
                    responseMessage.setReplyMarkup(getTimePeriodMenuReplyMarkup());
                }
            }
            case WAITING_FOR_REMINDER_PLACE_NAME -> {
                chatState.setPlaceName(text);
                chatState.setBotState(BotState.WAITING_FOR_REMINDER_TIME);
                chatStateRepository.save(chatState);
                responseMessage.setText("Введите время (в UTC), когда должно присылаться " +
                        "напоминание прогноза (пример: 08:00)");
                responseMessage.setReplyMarkup(getCancelReplyMarkup());
            }
            case WAITING_FOR_REMINDER_TIME -> {
                String response = handleNewSubscription(chatId, chatState.getPlaceName(), text);
                responseMessage.setText(response);
                if (!response.equals(BotConstants.WRONG_REMINDER_TIME)) {
                    chatState.setBotState(BotState.INITIAL);
                    chatState.setPlaceName(null);
                    chatStateRepository.save(chatState);
                }
            }
            case WAITING_FOR_REMINDER_POSITION_TO_DELETE -> {
                responseMessage.setText(handleDeleteSubscription(chatId, text));
                chatState.setBotState(BotState.INITIAL);
                chatStateRepository.save(chatState);
            }
        }
        return responseMessage;
    }

    /**
     * Обрабатывает запрос на получение прогноза погоды по часам на сегодня и возвращает ответ в виде строки
     *
     * @param placeName название места
     * @return ответ в виде строки
     */
    private String handleTodayForecasts(String placeName) {
        List<WeatherForecast> todayForecasts = weatherService.getForecast(placeName, 1);
        if (todayForecasts == null) {
            return BotConstants.NOT_FOUND_PLACE;
        }
        return forecastFormatter.formatTodayForecast(todayForecasts);
    }

    /**
     * Обрабатывает запрос на получение прогноза погоды по часам на завтра и возвращает ответ в виде строки
     *
     * @param placeName название места
     * @return ответ в виде строки
     */
    private String handleTomorrowForecasts(String placeName) {
        List<WeatherForecast> forecasts = weatherService.getForecast(placeName, 2);
        if (forecasts == null) {
            return BotConstants.NOT_FOUND_PLACE;
        }
        List<WeatherForecast> tomorrowForecasts = forecasts.subList(24, 48);
        return forecastFormatter.formatTomorrowForecast(tomorrowForecasts);
    }

    /**
     * Обрабатывает запрос на получение прогноза погоды на каждые 4 часа этой недели и возвращает ответ в виде строки
     *
     * @param placeName название места
     * @return ответ в виде строки
     */
    private String handleWeekForecasts(String placeName) {
        List<WeatherForecast> weekForecasts = weatherService.getForecast(placeName, 7);
        if (weekForecasts == null) {
            return BotConstants.NOT_FOUND_PLACE;
        }
        return forecastFormatter.formatWeekForecast(weekForecasts);
    }

    /**
     * Генерирует встроенную разметку клавиатуры для главного меню
     *
     * @return встроенная разметка клавиатуры для главного меню
     */
    private InlineKeyboardMarkup getMainMenuReplyMarkup() {
        InlineKeyboardMarkup mainMenuMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton forecastButton = new InlineKeyboardButton(BotConstants.FORECAST_BUTTON_TEXT);
        forecastButton.setCallbackData(BotConstants.FORECAST_BUTTON_TEXT);
        InlineKeyboardButton helpButton = new InlineKeyboardButton(BotConstants.HELP_BUTTON_TEXT);
        helpButton.setCallbackData(BotConstants.COMMAND_HELP);
        InlineKeyboardButton cancelButton = new InlineKeyboardButton(BotConstants.CANCEL_BUTTON_TEXT);
        cancelButton.setCallbackData(BotConstants.COMMAND_CANCEL);
        List<InlineKeyboardButton> firstRow = List.of(forecastButton);
        List<InlineKeyboardButton> secondRow = List.of(helpButton, cancelButton);
        List<List<InlineKeyboardButton>> keyboard = List.of(firstRow, secondRow);
        mainMenuMarkup.setKeyboard(keyboard);
        return mainMenuMarkup;
    }

    /**
     * Генерирует встроенную разметку клавиатуры для меню отмены
     *
     * @return встроенная разметка клавиатуры для меню отмены
     */
    private InlineKeyboardMarkup getCancelReplyMarkup() {
        InlineKeyboardMarkup cancelMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton cancelButton = new InlineKeyboardButton(BotConstants.CANCEL_BUTTON_TEXT);
        cancelButton.setCallbackData(BotConstants.COMMAND_CANCEL);
        List<InlineKeyboardButton> buttons = List.of(cancelButton);
        List<List<InlineKeyboardButton>> keyboard = List.of(buttons);
        cancelMarkup.setKeyboard(keyboard);
        return cancelMarkup;
    }

    /**
     * Генерирует встроенную разметку клавиатуры для меню периода времени
     *
     * @return встроенная разметка клавиатуры для меню периода времени
     */
    private InlineKeyboardMarkup getTimePeriodMenuReplyMarkup() {
        InlineKeyboardMarkup timePeriodMenu = new InlineKeyboardMarkup();
        InlineKeyboardButton todayButton = new InlineKeyboardButton(BotConstants.TODAY);
        todayButton.setCallbackData(BotConstants.TODAY);
        InlineKeyboardButton tomorrowButton = new InlineKeyboardButton(BotConstants.TOMORROW);
        tomorrowButton.setCallbackData(BotConstants.TOMORROW);
        InlineKeyboardButton weekButton = new InlineKeyboardButton(BotConstants.WEEK);
        weekButton.setCallbackData(BotConstants.WEEK);
        InlineKeyboardButton cancelButton = new InlineKeyboardButton(BotConstants.CANCEL_BUTTON_TEXT);
        cancelButton.setCallbackData(BotConstants.COMMAND_CANCEL);
        List<InlineKeyboardButton> firstRow = List.of(todayButton, tomorrowButton, weekButton);
        List<InlineKeyboardButton> secondRow = List.of(cancelButton);
        List<List<InlineKeyboardButton>> keyboard = List.of(firstRow, secondRow);
        timePeriodMenu.setKeyboard(keyboard);
        return timePeriodMenu;
    }

    /**
     * Обрабатывает запрос на добавление напоминания и возвращает ответ в виде строки
     *
     * @param chatId ID чата
     * @param placeName название места
     * @param time время в виде строки (в UTC)
     * @return ответ в виде строки
     */
    private String handleNewSubscription(long chatId, String placeName, String time) {
        try {
            reminderService.addReminder(chatId, placeName, time);
        } catch (IllegalArgumentException e) {
            return BotConstants.WRONG_REMINDER_TIME;
        }
        return BotConstants.ADDED_SUBSCRIPTION + " " + time;
    }

    /**
     * Обрабатывает запрос на удаление напоминания и возвращает ответ в виде строки
     *
     * @param chatId ID чата
     * @param position относительная позиция напоминания в списке
     * @return ответ в виде строки
     */
    private String handleDeleteSubscription(long chatId, String position) {
        try {
            int positionAsNumber = Integer.parseInt(position);
            reminderService.deleteReminderByRelativePosition(chatId, positionAsNumber);
            return BotConstants.DELETED_SUBSCRIPTION;
        } catch (NumberFormatException e) {
            return BotConstants.NOT_A_NUMBER_REMINDER_POSITION;
        } catch (IllegalArgumentException e) {
            return BotConstants.NO_REMINDER_WITH_POSITION;
        }
    }
}
