package ru.urfu.weatherforecastbot.bot.state;

import ru.urfu.weatherforecastbot.bot.state.handler.*;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.service.ReminderService;
import ru.urfu.weatherforecastbot.service.WeatherForecastRequestHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Контейнер обработчиков команд
 */
public class StateHandlerContainer {

    /**
     * Обработчики команд
     */
    private final Map<BotState, StateHandler> stateHandlers = new HashMap<>();

    /**
     * Создает контейнер обработчиков команд, используя переданные аргументы
     *
     * @param weatherForecastRequestHandler обработчик запросов прогнозы погоды
     * @param botStateManager               менеджер состояний бота
     * @param chatContextRepository         репозиторий контекстов чатов
     * @param reminderService               сервис для управления напоминаниями
     */
    public StateHandlerContainer(WeatherForecastRequestHandler weatherForecastRequestHandler,
                                 BotStateManager botStateManager,
                                 ChatContextRepository chatContextRepository,
                                 ReminderService reminderService) {
        stateHandlers.put(BotState.INITIAL, new InitialStateHandler(botStateManager));
        stateHandlers.put(BotState.WAITING_FOR_PLACE_NAME,
                new WaitingForPlaceNameStateHandler(botStateManager, chatContextRepository));
        stateHandlers.put(BotState.WAITING_FOR_TIME_PERIOD,
                new WaitingForTimePeriodStateHandler(weatherForecastRequestHandler,
                        botStateManager, chatContextRepository));
        stateHandlers.put(BotState.WAITING_FOR_TODAY_FORECAST_PLACE_NAME,
                new WaitingForTodayPlaceNameStateHandler(weatherForecastRequestHandler,
                        botStateManager, chatContextRepository));
        stateHandlers.put(BotState.WAITING_FOR_WEEK_FORECAST_PLACE_NAME, new
                WaitingForWeekPlaceNameStateHandler(weatherForecastRequestHandler,
                botStateManager, chatContextRepository));
        stateHandlers.put(BotState.WAITING_FOR_ADD_REMINDER_PLACE_NAME,
                new WaitingForAddReminderPlaceNameStateHandler(botStateManager, chatContextRepository));
        stateHandlers.put(BotState.WAITING_FOR_ADD_REMINDER_TIME,
                new WaitingForAddReminderTimeStateHandler(reminderService, botStateManager, chatContextRepository));
        stateHandlers.put(BotState.WAITING_FOR_REMINDER_POSITION_TO_DELETE,
                new WaitingForReminderPositionToDeleteStateHandler(reminderService, botStateManager));
    }

    /**
     * Возвращает обработчик, соответствующий указанному состоянию
     *
     * @param botState состояние бота
     * @return обработчик состояния
     */
    public StateHandler findStateHandler(BotState botState) {
        return stateHandlers.get(botState);
    }

}
