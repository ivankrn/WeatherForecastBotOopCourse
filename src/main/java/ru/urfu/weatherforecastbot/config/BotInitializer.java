package ru.urfu.weatherforecastbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.urfu.weatherforecastbot.bot.WeatherForecastBot;

/**
 * Инициализатор бота
 */
@Configuration
public class BotInitializer {

    /**
     * Бот для получения прогноза погоды
     */
    private final WeatherForecastBot bot;
    /**
     * Логгер
     */
    private final Logger logger = LoggerFactory.getLogger(BotInitializer.class);

    /**
     * Создаёт экземпляр BotInitializer с указанным ботом
     *
     * @param bot бот прогноза погоды
     */
    @Autowired
    public BotInitializer(WeatherForecastBot bot) {
        this.bot = bot;
    }

    /**
     * Инициализирует бота при запуске приложения
     */
    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage());
        }
    }

}
