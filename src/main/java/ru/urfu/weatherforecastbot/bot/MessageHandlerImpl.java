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

    private final WeatherForecastService weatherService;
    private final WeatherForecastFormatter forecastFormatter;

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
            if (command.equals("/info")) {
                String place = receivedText[1];
                responseMessage.setText(getTodayForecasts(place));
                // TODO: 05.11.2023 Добавить отправку прогноза на неделю, команды /start и /help
            }
        }
        return responseMessage;
    }

    /**
     * Получает прогноз погоды по часам на сегодня и возвращает его в виде строки
     *
     * @param placeName название места
     * @return прогноз погоды в виде строки
     */
    private String getTodayForecasts(String placeName) {
        List<WeatherForecast> todayForecasts = weatherService.getForecast(placeName, 1);
        return forecastFormatter.formatTodayForecast(todayForecasts);
    }

}
