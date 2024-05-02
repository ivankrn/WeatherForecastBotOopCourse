package ru.urfu.weatherforecastbot.bot.state;

import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.database.ChatStateRepository;
import ru.urfu.weatherforecastbot.model.ChatState;
import ru.urfu.weatherforecastbot.service.ReminderService;
import ru.urfu.weatherforecastbot.service.WeatherForecastRequestHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Менеджер состояний бота
 */
public class BotStateManager {

    /**
     * Переходы
     */
    private final Set<Transition> allowedTransitions = new HashSet<>();
    /**
     * Репозиторий состояний чатов
     */
    private final ChatStateRepository chatStateRepository;
    /**
     * Контейнер обработчиков команд
     */
    private final StateHandlerContainer stateHandlerContainer;

    /**
     * Создает экземпляр {@link BotStateManager}, используя переданные аргументы
     *
     * @param weatherForecastRequestHandler обработчик запросов прогнозов погоды
     * @param chatStateRepository           репозиторий состояний чатов
     * @param chatContextRepository         репозиторий контекстов чатов
     * @param reminderService               сервис для управления напоминаниями
     */
    public BotStateManager(WeatherForecastRequestHandler weatherForecastRequestHandler,
                           ChatStateRepository chatStateRepository,
                           ChatContextRepository chatContextRepository,
                           ReminderService reminderService) {
        this.chatStateRepository = chatStateRepository;
        stateHandlerContainer = new StateHandlerContainer(weatherForecastRequestHandler, this,
                chatContextRepository, reminderService);
        initTransitions();
    }

    /**
     * Обрабатывает сообщение пользователя и возвращает ответное сообщение
     *
     * @param chatId      ID чата
     * @param userMessage сообщение пользователя
     * @return ответное сообщение
     */
    public BotMessage handle(long chatId, String userMessage) {
        ChatState chatState = chatStateRepository.findById(chatId).orElseGet(() -> {
            ChatState newChatState = new ChatState();
            newChatState.setChatId(chatId);
            newChatState.setBotState(BotState.INITIAL);
            return chatStateRepository.save(newChatState);
        });
        BotState currentState = chatState.getBotState();
        return stateHandlerContainer
                .findStateHandler(currentState).handle(chatId, userMessage);
    }

    /**
     * Иницирует переход к следующему состоянию, если переход допустим
     *
     * @param chatId ID чата
     * @param next   следующее состояние
     */
    public void nextState(long chatId, BotState next) {
        ChatState chatState = chatStateRepository.findById(chatId).orElseGet(() -> {
            ChatState newChatState = new ChatState();
            newChatState.setChatId(chatId);
            newChatState.setBotState(BotState.INITIAL);
            return chatStateRepository.save(newChatState);
        });
        BotState currentBotState = chatState.getBotState();
        Transition transition = new Transition(currentBotState, next);
        if (allowedTransitions.contains(transition)) {
            chatState.setBotState(next);
            chatStateRepository.save(chatState);
        }
    }

    /**
     * Инициализирует допустимые переходы
     */
    private void initTransitions() {
        allowedTransitions.add(new Transition(BotState.INITIAL, BotState.WAITING_FOR_TODAY_FORECAST_PLACE_NAME));
        allowedTransitions.add(new Transition(BotState.INITIAL, BotState.WAITING_FOR_WEEK_FORECAST_PLACE_NAME));
        allowedTransitions.add(new Transition(BotState.INITIAL, BotState.WAITING_FOR_PLACE_NAME));
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_TODAY_FORECAST_PLACE_NAME, BotState.INITIAL));
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_WEEK_FORECAST_PLACE_NAME, BotState.INITIAL));
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_PLACE_NAME, BotState.WAITING_FOR_TIME_PERIOD));
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_TIME_PERIOD, BotState.INITIAL));
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_PLACE_NAME, BotState.INITIAL));
        allowedTransitions.add(new Transition(BotState.INITIAL, BotState.WAITING_FOR_ADD_REMINDER_PLACE_NAME));
        allowedTransitions.add(
                new Transition(BotState.WAITING_FOR_ADD_REMINDER_PLACE_NAME, BotState.WAITING_FOR_ADD_REMINDER_TIME));
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_ADD_REMINDER_TIME, BotState.INITIAL));
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_ADD_REMINDER_PLACE_NAME, BotState.INITIAL));
        allowedTransitions.add(new Transition(BotState.INITIAL, BotState.WAITING_FOR_REMINDER_POSITION_TO_DELETE));
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_REMINDER_POSITION_TO_DELETE, BotState.INITIAL));
        allowedTransitions.add(new Transition(BotState.INITIAL, BotState.WAITING_FOR_REMINDER_POSITION_TO_EDIT));
        allowedTransitions.add(
                new Transition(BotState.WAITING_FOR_REMINDER_POSITION_TO_EDIT,
                        BotState.WAITING_FOR_EDIT_REMINDER_PLACE_NAME)
        );
        allowedTransitions.add(
                new Transition(BotState.WAITING_FOR_EDIT_REMINDER_PLACE_NAME,
                        BotState.WAITING_FOR_EDIT_REMINDER_TIME)
        );
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_EDIT_REMINDER_TIME, BotState.INITIAL));
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_REMINDER_POSITION_TO_EDIT, BotState.INITIAL));
        allowedTransitions.add(new Transition(BotState.WAITING_FOR_EDIT_REMINDER_PLACE_NAME, BotState.INITIAL));
    }

}
