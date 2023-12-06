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
     * Создает экземпляр MessageHandlerImpl, используя в качестве {@link MessageHandlerImpl#forecastFormatter
     * forecastFormatter} {@link WeatherForecastFormatterImpl}
     *
     * @param weatherService сервис для получения прогнозов погоды
     * @param chatStateRepository репозиторий состояний чатов
     */
    @Autowired
    public MessageHandlerImpl(WeatherForecastService weatherService, ChatStateRepository chatStateRepository) {
        this.weatherService = weatherService;
        forecastFormatter = new WeatherForecastFormatterImpl();
        this.chatStateRepository = chatStateRepository;
    }

    /**
     * Создает экземпляр MessageHandlerImpl, используя переданные аргументы
     *
     * @param weatherService    сервис для получения прогнозов погоды
     * @param forecastFormatter форматировщик прогноза погоды в удобочитаемый вид
     * @param chatStateRepository репозиторий состояний чатов
     */
    public MessageHandlerImpl(WeatherForecastService weatherService,
                              WeatherForecastFormatterImpl forecastFormatter, ChatStateRepository chatStateRepository) {
        this.weatherService = weatherService;
        this.forecastFormatter = forecastFormatter;
        this.chatStateRepository = chatStateRepository;
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
                    chatState.setTimePeriod(null);
                    chatStateRepository.save(chatState);
                    responseMessage.setText(BotConstants.START_TEXT);
                    responseMessage.setReplyMarkup(getMainMenuReplyMarkup());
                }
                case BotConstants.COMMAND_HELP -> responseMessage.setText(BotConstants.HELP_TEXT);
                case BotConstants.COMMAND_CANCEL -> {
                    chatState.setBotState(BotState.INITIAL);
                    chatState.setPlaceName(null);
                    chatState.setTimePeriod(null);
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
                if (text.equals(BotConstants.FORECAST_BUTTON_TEXT)
                        || text.equals(BotConstants.COMMAND_FORECAST_TODAY)
                        || text.equals(BotConstants.COMMAND_FORECAST_WEEK)) {
                    chatState.setBotState(BotState.WAITING_FOR_PLACE_NAME);
                    if (text.equals(BotConstants.COMMAND_FORECAST_TODAY)) {
                        chatState.setTimePeriod(BotConstants.TODAY);
                    } else if (text.equals(BotConstants.COMMAND_FORECAST_WEEK)) {
                        chatState.setTimePeriod(BotConstants.WEEK);
                    }
                    chatStateRepository.save(chatState);
                    responseMessage.setText("Введите название места");
                    responseMessage.setReplyMarkup(getCancelReplyMarkup());
                } else {
                    responseMessage.setText(BotConstants.UNKNOWN_COMMAND);
                }
            }
            case WAITING_FOR_PLACE_NAME -> {
                chatState.setPlaceName(text);
                if (chatState.getTimePeriod().isPresent()) {
                    String timePeriod = chatState.getTimePeriod().get();
                    if (timePeriod.equals(BotConstants.TODAY)) {
                        responseMessage.setText(handleTodayForecasts(chatState.getPlaceName()));
                    } else if (timePeriod.equals(BotConstants.WEEK)) {
                        responseMessage.setText(handleWeekForecasts(chatState.getPlaceName()));
                    }
                    chatState.setBotState(BotState.INITIAL);
                    chatState.setPlaceName(null);
                    chatState.setTimePeriod(null);
                } else {
                    chatState.setBotState(BotState.WAITING_FOR_TIME_PERIOD);
                    responseMessage.setText("Выберите временной период для просмотра (сегодня, завтра, неделя)");
                    responseMessage.setReplyMarkup(getTimePeriodMenuReplyMarkup());
                }
                chatStateRepository.save(chatState);
            }
            case WAITING_FOR_TIME_PERIOD -> {
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
        helpButton.setCallbackData(BotCommands.COMMAND_HELP);
        InlineKeyboardButton cancelButton = new InlineKeyboardButton(BotConstants.CANCEL_BUTTON_TEXT);
        cancelButton.setCallbackData(BotCommands.COMMAND_CANCEL);
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
        cancelButton.setCallbackData(BotCommands.COMMAND_CANCEL);
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
        cancelButton.setCallbackData(BotCommands.COMMAND_CANCEL);
        List<InlineKeyboardButton> firstRow = List.of(todayButton, tomorrowButton, weekButton);
        List<InlineKeyboardButton> secondRow = List.of(cancelButton);
        List<List<InlineKeyboardButton>> keyboard = List.of(firstRow, secondRow);
        timePeriodMenu.setKeyboard(keyboard);
        return timePeriodMenu;
    }
}
