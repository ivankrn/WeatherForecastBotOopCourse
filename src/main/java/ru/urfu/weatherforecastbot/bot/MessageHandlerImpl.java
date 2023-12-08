package ru.urfu.weatherforecastbot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urfu.weatherforecastbot.database.ChatStateRepository;
import ru.urfu.weatherforecastbot.model.BotState;
import ru.urfu.weatherforecastbot.model.ChatState;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.ForecastTimePeriod;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;
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
    private final WeatherForecastFormatter forecastFormatter;
    /**
     * Репозиторий состояний чатов
     */
    private final ChatStateRepository chatStateRepository;

    /**
     * Создает экземпляр MessageHandlerImpl, используя в качестве {@link MessageHandlerImpl#forecastFormatter
     * forecastFormatter} {@link WeatherForecastFormatterImpl}
     *
     * @param weatherService      сервис для получения прогнозов погоды
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
     * @param weatherService      сервис для получения прогнозов погоды
     * @param forecastFormatter   форматировщик прогноза погоды в удобочитаемый вид
     * @param chatStateRepository репозиторий состояний чатов
     */
    public MessageHandlerImpl(WeatherForecastService weatherService,
                              WeatherForecastFormatter forecastFormatter, ChatStateRepository chatStateRepository) {
        this.weatherService = weatherService;
        this.forecastFormatter = forecastFormatter;
        this.chatStateRepository = chatStateRepository;
    }

    @Override
    public BotMessage handle(long chatId, String message) {
        BotMessage responseMessage = new BotMessage();
        ChatState chatState = chatStateRepository.findById(chatId).orElseGet(() -> {
            ChatState newChatState = new ChatState();
            newChatState.setChatId(chatId);
            newChatState.setBotState(BotState.INITIAL);
            return chatStateRepository.save(newChatState);
        });
        String[] splittedText = message.split(" ");
        String command = splittedText[0];
        switch (command) {
            case BotConstants.COMMAND_START -> {
                chatState.setBotState(BotState.INITIAL);
                chatState.setPlaceName(null);
                chatState.setTimePeriod(null);
                chatStateRepository.save(chatState);
                responseMessage.setText(BotConstants.START_TEXT);
                responseMessage.setButtons(getMainMenuButtons());
            }
            case BotConstants.COMMAND_HELP -> responseMessage.setText(BotConstants.HELP_TEXT);
            case BotConstants.COMMAND_CANCEL -> {
                chatState.setBotState(BotState.INITIAL);
                chatState.setPlaceName(null);
                chatState.setTimePeriod(null);
                chatStateRepository.save(chatState);
                responseMessage.setText("Вы вернулись в основное меню");
                responseMessage.setButtons(getMainMenuButtons());
            }
            case BotConstants.COMMAND_FORECAST_TODAY -> {
                if (splittedText.length < 2) {
                    return handleNonCommand(chatId, command);
                } else {
                    String place = message.substring(message.indexOf(" ") + 1);
                    responseMessage.setText(handleTodayForecasts(place));
                }
            }
            case BotConstants.COMMAND_FORECAST_WEEK -> {
                if (splittedText.length < 2) {
                    return handleNonCommand(chatId, command);
                } else {
                    String place = message.substring(message.indexOf(" ") + 1);
                    responseMessage.setText(handleWeekForecasts(place));
                }
            }
            default -> {
                return handleNonCommand(chatId, message);
            }
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
    private BotMessage handleNonCommand(long chatId, String text) {
        BotMessage responseMessage = new BotMessage();
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
                    responseMessage.setButtons(getCancelMenuButtons());
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
                    responseMessage.setButtons(getTimePeriodMenuButtons());
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
                    responseMessage.setButtons(getTimePeriodMenuButtons());
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
        if (todayForecasts.isEmpty()) {
            return BotConstants.NOT_FOUND_PLACE;
        }
        return forecastFormatter.formatForecasts(ForecastTimePeriod.TODAY, todayForecasts);
    }

    /**
     * Обрабатывает запрос на получение прогноза погоды по часам на завтра и возвращает ответ в виде строки
     *
     * @param placeName название места
     * @return ответ в виде строки
     */
    private String handleTomorrowForecasts(String placeName) {
        List<WeatherForecast> forecasts = weatherService.getForecast(placeName, 2);
        if (forecasts.isEmpty()) {
            return BotConstants.NOT_FOUND_PLACE;
        }
        List<WeatherForecast> tomorrowForecasts = forecasts.subList(24, 48);
        return forecastFormatter.formatForecasts(ForecastTimePeriod.TOMORROW, tomorrowForecasts);
    }

    /**
     * Обрабатывает запрос на получение прогноза погоды на каждые 4 часа этой недели и возвращает ответ в виде строки
     *
     * @param placeName название места
     * @return ответ в виде строки
     */
    private String handleWeekForecasts(String placeName) {
        List<WeatherForecast> weekForecasts = weatherService.getForecast(placeName, 7);
        if (weekForecasts.isEmpty()) {
            return BotConstants.NOT_FOUND_PLACE;
        }
        return forecastFormatter.formatForecasts(ForecastTimePeriod.WEEK, weekForecasts);
    }

    /**
     * Генерирует кнопки для главного меню
     *
     * @return кнопки для главного меню
     */
    private List<Button> getMainMenuButtons() {
        Button forecastButton = new Button();
        forecastButton.setText(BotConstants.FORECAST_BUTTON_TEXT);
        forecastButton.setCallback(BotConstants.FORECAST_BUTTON_TEXT);
        Button helpButton = new Button();
        helpButton.setText(BotConstants.HELP_BUTTON_TEXT);
        helpButton.setCallback(BotConstants.COMMAND_HELP);
        Button cancelButton = new Button();
        cancelButton.setText(BotConstants.CANCEL_BUTTON_TEXT);
        cancelButton.setCallback(BotConstants.COMMAND_CANCEL);
        return List.of(forecastButton, helpButton, cancelButton);
    }

    /**
     * Генерирует кнопки для меню отмены
     *
     * @return кнопки для меню отмены
     */
    private List<Button> getCancelMenuButtons() {
        Button cancelButton = new Button();
        cancelButton.setText(BotConstants.CANCEL_BUTTON_TEXT);
        cancelButton.setCallback(BotConstants.COMMAND_CANCEL);
        return List.of(cancelButton);
    }

    /**
     * Генерирует кнопки для меню периода времени
     *
     * @return кнопки для меню периода времени
     */
    private List<Button> getTimePeriodMenuButtons() {
        Button todayButton = new Button();
        todayButton.setText(BotConstants.TODAY);
        todayButton.setCallback(BotConstants.TODAY);
        Button tomorrowButton = new Button();
        tomorrowButton.setText(BotConstants.TOMORROW);
        tomorrowButton.setCallback(BotConstants.TOMORROW);
        Button weekButton = new Button();
        weekButton.setText(BotConstants.WEEK);
        weekButton.setCallback(BotConstants.WEEK);
        Button cancelButton = new Button();
        cancelButton.setText(BotConstants.CANCEL_BUTTON_TEXT);
        cancelButton.setCallback(BotConstants.COMMAND_CANCEL);
        return List.of(todayButton, tomorrowButton, weekButton, cancelButton);
    }
}
