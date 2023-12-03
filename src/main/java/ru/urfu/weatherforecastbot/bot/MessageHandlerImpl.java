package ru.urfu.weatherforecastbot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
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
     * Создает экземпляр MessageHandlerImpl, используя в качестве {@link MessageHandlerImpl#forecastFormatter
     * forecastFormatter} {@link WeatherForecastFormatterImpl}
     *
     * @param weatherService сервис для получения прогнозов погоды
     */
    @Autowired
    public MessageHandlerImpl(WeatherForecastService weatherService) {
        this.weatherService = weatherService;
        forecastFormatter = new WeatherForecastFormatterImpl();
    }

    /**
     * Создает экземпляр MessageHandlerImpl, используя переданные аргументы
     *
     * @param weatherService сервис для получения прогнозов погоды
     * @param forecastFormatter форматировщик прогноза погоды в удобочитаемый вид
     */
    public MessageHandlerImpl(WeatherForecastService weatherService, WeatherForecastFormatterImpl forecastFormatter) {
        this.weatherService = weatherService;
        this.forecastFormatter = forecastFormatter;
    }

    @Override
    public SendMessage handle(Message message) {
        long chatId = message.getChatId();
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);
        if (message.hasText()) {
            String[] receivedText = message.getText().split(" ");
            String command = receivedText[0];

            switch (command) {
                case BotConstants.COMMAND_START -> responseMessage.setText(BotConstants.START_TEXT);
                case BotConstants.COMMAND_HELP -> responseMessage.setText(BotConstants.HELP_TEXT);
                case BotConstants.COMMAND_FORECAST_TODAY -> {
                    if (receivedText.length < 2) {
                        responseMessage.setText(BotConstants.WRONG_COMMAND_SYNTAX);
                    } else {
                        String place = message.getText().substring(message.getText().indexOf(" ") + 1);
                        responseMessage.setText(handleTodayForecasts(place));
                    }
                }
                case BotConstants.COMMAND_FORECAST_WEEK -> {
                    if (receivedText.length < 2) {
                        responseMessage.setText(BotConstants.WRONG_COMMAND_SYNTAX);
                    } else {
                        String place = message.getText().substring(message.getText().indexOf(" ") + 1);
                        responseMessage.setText(handleWeekForecasts(place));
                    }
                }
                default -> responseMessage.setText(BotConstants.UNKNOWN_COMMAND);
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
}
