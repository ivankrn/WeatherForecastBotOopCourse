package ru.urfu.weatherforecastbot.bot;

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

/**
 * Бот для получения прогноза погоды
 */
@Component
public class WeatherForecastBot extends TelegramLongPollingBot {

    /**
     * Конфигурация бота
     */
    private final BotConfig botConfig;
    /**
     * Обработчик сообщений
     */
    private final MessageHandler messageHandler;
    /**
     * Логгер
     */
    private final Logger logger = LoggerFactory.getLogger(WeatherForecastBot.class);

    @Autowired
    public WeatherForecastBot(BotConfig botConfig, MessageHandler messageHandler) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.messageHandler = messageHandler;
    }

    /**
     * Обработчик событий Telegram
     *
     * @param update событие
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            SendMessage responseMessage = messageHandler.handle(update.getMessage());
            executeMessageWithLogging(responseMessage);
        }
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
     * Выполняет сообщение Telegram с логированием
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
