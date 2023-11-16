package ru.urfu.weatherforecastbot.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;

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
     * Создает экземпляр MessageHandlerImpl, используя переданные аргументы
     *
     * @param weatherService сервис для получения прогнозов погоды
     * @param forecastFormatter форматировщик прогноза погоды в удобочитаемый вид
     */
    public MessageHandlerImpl(WeatherForecastService weatherService,
                              WeatherForecastFormatter forecastFormatter) {
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
                case "/start" -> responseMessage.setText(BotText.START_COMMAND.text);
                case "/help" -> responseMessage.setText(BotText.HELP_COMMAND.text);
                case "/info" -> {
                    if (receivedText.length < 2) {
                        responseMessage.setText(BotText.WRONG_COMMAND_SYNTAX.text);
                    } else {
                        String place = receivedText[1];
                        responseMessage.setText(handleTodayForecasts(place));
                    }
                }
                case "/info_week" -> {
                    if (receivedText.length < 2) {
                        responseMessage.setText(BotText.WRONG_COMMAND_SYNTAX.text);
                    } else {
                        String place = receivedText[1];
                        responseMessage.setText(handleWeekForecasts(place));
                    }
                }
                default -> responseMessage.setText(BotText.UNKNOWN_COMMAND.text);
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
            return BotText.NOT_FOUND.text;
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
        List<WeatherForecast> todayForecasts = weatherService.getForecast(placeName, 7);
        if (todayForecasts == null) {
            return BotText.NOT_FOUND.text;
        }
        return forecastFormatter.formatWeekForecast(todayForecasts);
    }
}
