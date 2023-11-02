package ru.urfu.weatherforecastbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.urfu.weatherforecastbot.config.BotConfig;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;

import java.util.List;

/**
 * Бот для получения прогноза погоды
 */
@Component
public class WeatherForecastBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final WeatherForecastService weatherService;
    private final WeatherForecastFormatter forecastFormatter;
    private final Logger logger = LoggerFactory.getLogger(WeatherForecastBot.class);

    @Autowired
    public WeatherForecastBot(BotConfig botConfig, WeatherForecastService weatherService,
                              WeatherForecastFormatter forecastFormatter) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.weatherService = weatherService;
        this.forecastFormatter = forecastFormatter;
    }

    /**
     * Обработчик событий (сообщений и прочего) Telegram
     *
     * @param update событие
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String[] receivedText = update.getMessage().getText().split(" ");
            String command = receivedText[0];
            if (command.equals("/info")) {
                String place = receivedText[1];
                sendTodayForecasts(place, chatId);
            }
        }
    }

    /**
     * Отправляет прогноз погоды по часам на сегодня в чат с указанным id
     *
     * @param placeName название места
     * @param chatId id чата
     */
    private void sendTodayForecasts(String placeName, long chatId) {
        List<WeatherForecast> todayForecasts = weatherService.getForecast(placeName, 1);
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(forecastFormatter.formatTodayForecast(todayForecasts));
        executeMessageWithLogging(message);
    }

    /**
     * Возвращает название бота
     *
     * @return название бота
     */
    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    /**
     * Выполняет сообщение Telegram с логированием.
     *
     * @param message сообщение
     */
    private void executeMessageWithLogging(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage());
        }
    }
}
