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
import ru.urfu.weatherforecastbot.WeatherForecastBot;

/**
 * Инициализатор бота
 */
@Configuration
public class BotInitializer {

    private final WeatherForecastBot bot;
    private final Logger logger = LoggerFactory.getLogger(BotInitializer.class);

    @Autowired
    public BotInitializer(WeatherForecastBot bot) {
        this.bot = bot;
    }

    /**
     * Инициализирует бота
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
