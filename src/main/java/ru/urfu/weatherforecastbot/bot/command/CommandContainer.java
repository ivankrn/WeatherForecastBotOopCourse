package ru.urfu.weatherforecastbot.bot.command;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.command.handler.*;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.service.WeatherForecastRequestHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Контейнер обработчиков команд
 */
public class CommandContainer {

    /**
     * Обработчики команд
     */
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    /**
     * Словарь, хранящий требуемое количество аргументов для команд
     */
    private final Map<String, Integer> commandRequiredArgsCount = new HashMap<>();

    /**
     * Создает экземпляр {@link CommandContainer}, используя переданные аргументы
     *
     * @param weatherForecastRequestHandler обработчик запросов прогнозов погоды
     * @param chatContextRepository         репозиторий контекстов чатов
     * @param botStateManager               менеджер состояний бота
     */
    public CommandContainer(WeatherForecastRequestHandler weatherForecastRequestHandler,
                            ChatContextRepository chatContextRepository,
                            BotStateManager botStateManager) {
        addCommandHandler(BotConstants.COMMAND_START,
                new StartCommandHandler(chatContextRepository, botStateManager), 0);
        addCommandHandler(BotConstants.COMMAND_HELP, new HelpCommandHandler(), 0);
        addCommandHandler(BotConstants.COMMAND_FORECAST_TODAY,
                new ForecastTodayCommandHandler(weatherForecastRequestHandler), 1);
        addCommandHandler(BotConstants.COMMAND_FORECAST_WEEK,
                new ForecastWeekCommandHandler(weatherForecastRequestHandler), 1);
        addCommandHandler(BotConstants.COMMAND_CANCEL,
                new CancelCommandHandler(chatContextRepository, botStateManager), 0);
    }

    /**
     * Проверяет возможность обработки сообщения пользователя
     *
     * @param message сообщения пользователя
     * @return true, если может обработать сообщение, иначе false
     */
    public boolean canHandle(String message) {
        String[] splittedText = message.split(" ");
        String command = splittedText[0];
        int argsCount = splittedText.length - 1;
        return commandHandlers.containsKey(command)
                && argsCount >= commandRequiredArgsCount.getOrDefault(command, 0);
    }

    /**
     * Возвращает обработчик, соответствующий команде пользователя
     *
     * @param message сообщение пользователя
     * @return обработчик команды
     */
    public CommandHandler findCommandHandler(String message) {
        String[] splittedText = message.split(" ");
        String command = splittedText[0];
        return commandHandlers.get(command);
    }

    /**
     * Добавляет обработчик команды
     *
     * @param command           команда
     * @param commandHandler    обработчик команды
     * @param requiredArgsCount требуемое количество аргументов для команды
     */
    public void addCommandHandler(String command, CommandHandler commandHandler, int requiredArgsCount) {
        commandHandlers.put(command, commandHandler);
        commandRequiredArgsCount.put(command, requiredArgsCount);
    }
}
