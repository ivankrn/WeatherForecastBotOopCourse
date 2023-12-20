package ru.urfu.weatherforecastbot.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urfu.weatherforecastbot.bot.command.CommandContainer;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.database.ChatStateRepository;
import ru.urfu.weatherforecastbot.service.ReminderService;
import ru.urfu.weatherforecastbot.service.WeatherForecastRequestHandler;
import ru.urfu.weatherforecastbot.service.WeatherForecastRequestHandlerImpl;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;

@Component
public class MessageHandlerImpl implements MessageHandler {

    private final BotStateManager botStateManager;
    private final CommandContainer commandContainer;

    /**
     * Создает экземпляр MessageHandlerImpl
     *
     * @param weatherService        сервис для получения прогнозов погоды
     * @param chatContextRepository репозиторий контекстов чатов
     * @param chatStateRepository   репозиторий состояний чатов
     * @param reminderService       сервис для управления напоминаниями
     */
    @Autowired
    public MessageHandlerImpl(WeatherForecastService weatherService,
                              ChatContextRepository chatContextRepository,
                              ChatStateRepository chatStateRepository,
                              ReminderService reminderService) {
        WeatherForecastRequestHandler weatherForecastRequestHandler =
                new WeatherForecastRequestHandlerImpl(weatherService);
        botStateManager = new BotStateManager(weatherForecastRequestHandler,
                chatStateRepository, chatContextRepository, reminderService);
        commandContainer = new CommandContainer(weatherForecastRequestHandler, chatContextRepository,
                botStateManager, reminderService);
    }

    /**
     * Создает экземпляр MessageHandlerImpl, используя переданные аргументы
     *
     * @param weatherService        сервис для получения прогнозов погоды
     * @param forecastFormatter     форматировщик прогноза погоды в удобочитаемый вид
     * @param chatContextRepository репозиторий контекстов чатов
     * @param chatStateRepository   репозиторий состояний чатов
     * @param reminderService       сервис для управления напоминаниями
     */
    public MessageHandlerImpl(WeatherForecastService weatherService,
                              WeatherForecastFormatter forecastFormatter,
                              ChatContextRepository chatContextRepository,
                              ChatStateRepository chatStateRepository,
                              ReminderService reminderService) {
        WeatherForecastRequestHandler weatherForecastRequestHandler =
                new WeatherForecastRequestHandlerImpl(weatherService, forecastFormatter);
        botStateManager = new BotStateManager(weatherForecastRequestHandler, chatStateRepository,
                chatContextRepository, reminderService);
        commandContainer = new CommandContainer(weatherForecastRequestHandler, chatContextRepository,
                botStateManager, reminderService);
    }

    @Override
    public BotMessage handle(long chatId, String message) {
        if (commandContainer.canHandle(message)) {
            return commandContainer.findCommandHandler(message).handle(chatId, message);
        } else {
            return botStateManager.handle(chatId, message);
        }
    }

}
