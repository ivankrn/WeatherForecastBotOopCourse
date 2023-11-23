package ru.urfu.weatherforecastbot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.urfu.weatherforecastbot.database.ChatStateRepository;
import ru.urfu.weatherforecastbot.model.BotState;
import ru.urfu.weatherforecastbot.model.ChatState;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.util.ArrayList;
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
     * @param weatherService сервис для получения прогнозов погоды
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
     * @param weatherService сервис для получения прогнозов погоды
     * @param forecastFormatter форматировщик прогноза погоды в удобочитаемый вид
     */
    public MessageHandlerImpl(WeatherForecastService weatherService,
                              WeatherForecastFormatter forecastFormatter, ChatStateRepository chatStateRepository) {
        this.weatherService = weatherService;
        this.forecastFormatter = forecastFormatter;
        this.chatStateRepository = chatStateRepository;
    }

    @Override
    public SendMessage handle(Message message) {
        long chatId = message.getChatId();
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);
        if (!chatStateRepository.existsById(chatId)) {
            ChatState newChatState = new ChatState();
            newChatState.setChatId(chatId);
            newChatState.setBotState(BotState.INITIAL);
            chatStateRepository.save(newChatState);
        }
        if (message.hasText()) {
            if (message.getText().startsWith("/")) {
                responseMessage.setText(handleCommand(message.getText()));
            } else {
                return handleNonCommand(chatId, message.getText());
            }
        } else {
            responseMessage.setText(BotText.UNDERSTAND_ONLY_TEXT.getText());
        }

        return responseMessage;
    }

    /**
     * Обрабатывает сообщение пользователя, если оно содержит команду, и возвращает ответ в виде строки
     *
     * @param text текст, присланный пользователем
     * @return ответ в виде строки
     */
    private String handleCommand(String text) {
        String[] splittedText = text.split(" ");
        String command = splittedText[0];
        switch (command) {
            case "/start" -> {
                return BotText.START_COMMAND.getText();
            }
            case "/help" -> {
                return BotText.HELP_COMMAND.getText();
            }
            case "/info" -> {
                if (splittedText.length < 2) {
                    return BotText.WRONG_COMMAND_SYNTAX.getText();
                } else {
                    String place = splittedText[1];
                    return handleTodayForecasts(place);
                }
            }
            case "/info_week" -> {
                if (splittedText.length < 2) {
                    return BotText.WRONG_COMMAND_SYNTAX.getText();
                } else {
                    String place = splittedText[1];
                    return handleWeekForecasts(place);
                }
            }
            default -> {
                return BotText.UNKNOWN_COMMAND.getText();
            }
        }
    }

    /**
     * Обрабатывает сообщение пользователя, если оно не содержит команду, и возвращает ответ в виде строки
     *
     * @param chatId ID чата
     * @param text текст, присланный пользователем
     * @return ответ в виде строки
     */
    private SendMessage handleNonCommand(long chatId, String text) {
        ChatState chatState = chatStateRepository.findById(chatId).get();
        BotState currentBotState = chatState.getBotState();

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();

        SendMessage responseMessage = new SendMessage();

        responseMessage.setChatId(chatId);

        if (text.equalsIgnoreCase("Отмена")) {
            chatState.setBotState(BotState.INITIAL);
            chatStateRepository.save(chatState);
            keyboardFirstRow.add("Прогноз");
            keyboardFirstRow.add("Помощь");
            keyboardFirstRow.add("Отмена");

            keyboard.add(keyboardFirstRow);
            replyKeyboardMarkup.setKeyboard(keyboard);
            responseMessage.setReplyMarkup(replyKeyboardMarkup);
            responseMessage.setText("Вы вернулись в основное меню");
            return responseMessage;
        } else {
            switch (currentBotState) {
                case INITIAL -> {
                    keyboardFirstRow.add("Прогноз");
                    keyboardFirstRow.add("Помощь");
                    keyboardFirstRow.add("Отмена");

                    keyboard.add(keyboardFirstRow);
                    replyKeyboardMarkup.setKeyboard(keyboard);
                    responseMessage.setReplyMarkup(replyKeyboardMarkup);
                    responseMessage.setText("Вы вернулись в основное меню");
                    if (text.equalsIgnoreCase("Прогноз")) {
                        chatState.setBotState(BotState.WAITING_FOR_PLACE_NAME);
                        chatStateRepository.save(chatState);
                        responseMessage.setText("Введите название места");
                        return responseMessage;
                    } else if (text.equalsIgnoreCase("Старт")) {
                        responseMessage.setText(BotText.START_COMMAND.getText());
                        return responseMessage;
                    } else if (text.equalsIgnoreCase("Помощь")) {
                        responseMessage.setText(BotText.HELP_COMMAND.getText());
                        return responseMessage;
                    } else {
                        responseMessage.setText(BotText.UNKNOWN_COMMAND.getText());
                        return responseMessage;
                    }
                }
                case WAITING_FOR_PLACE_NAME -> {
                    keyboardFirstRow.add("Сегодня");
                    keyboardFirstRow.add("Завтра");
                    keyboardFirstRow.add("Неделя");

                    keyboard.add(keyboardFirstRow);
                    replyKeyboardMarkup.setKeyboard(keyboard);
                    responseMessage.setReplyMarkup(replyKeyboardMarkup);
                    responseMessage.setText("Выберите временной период для просмотра (сегодня, завтра, неделя)");

                    chatState.setPlaceName(text);
                    chatState.setBotState(BotState.WAITING_FOR_TIME_PERIOD);
                    chatStateRepository.save(chatState);
                    return responseMessage;
                }
                case WAITING_FOR_TIME_PERIOD -> {
                    keyboardFirstRow.add("Сегодня");
                    keyboardFirstRow.add("Завтра");
                    keyboardFirstRow.add("Неделя");

                    keyboard.add(keyboardFirstRow);
                    replyKeyboardMarkup.setKeyboard(keyboard);
                    responseMessage.setReplyMarkup(replyKeyboardMarkup);

                    if (text.equalsIgnoreCase("Сегодня")) {
                        chatState.setBotState(BotState.INITIAL);
                        chatStateRepository.save(chatState);
                        responseMessage.setText(handleTodayForecasts(chatState.getPlaceName()));
                        return responseMessage;
                    } else if (text.equalsIgnoreCase("Завтра")) {
                        chatState.setBotState(BotState.INITIAL);
                        chatStateRepository.save(chatState);
                        responseMessage.setText(handleTomorrowForecasts(chatState.getPlaceName()));
                        return responseMessage;
                    } else if (text.equalsIgnoreCase("Неделя")) {
                        chatState.setBotState(BotState.INITIAL);
                        chatStateRepository.save(chatState);
                        responseMessage.setText(handleWeekForecasts(chatState.getPlaceName()));
                        return responseMessage;
                    } else {
                        responseMessage.setText("Введите корректный временной период. " +
                                "Допустимые значения: сегодня, завтра, неделя");
                        return responseMessage;
                    }
                }
                default -> {
                    responseMessage.setText(BotText.UNKNOWN_COMMAND.getText());
                    return responseMessage;
                }
            }
        }
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
            return BotText.NOT_FOUND.getText();
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
            return BotText.NOT_FOUND.getText();
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
            return BotText.NOT_FOUND.getText();
        }
        return forecastFormatter.formatWeekForecast(weekForecasts);
    }
}
