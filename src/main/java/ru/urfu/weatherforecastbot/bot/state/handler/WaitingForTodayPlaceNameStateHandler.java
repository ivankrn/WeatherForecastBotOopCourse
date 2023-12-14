package ru.urfu.weatherforecastbot.bot.state.handler;

import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.state.BotState;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.model.ChatContext;
import ru.urfu.weatherforecastbot.service.WeatherForecastRequestHandler;
import ru.urfu.weatherforecastbot.util.ForecastTimePeriod;

/**
 * Обработчик состояния ожидания названия места для прогноза на сегодня
 */
public class WaitingForTodayPlaceNameStateHandler implements StateHandler {

    /**
     * Обработчик запросов прогнозы погоды
     */
    private final WeatherForecastRequestHandler weatherForecastRequestHandler;
    /**
     * Менеджер состояний бота
     */
    private final BotStateManager botStateManager;
    /**
     * Репозиторий контекстов чатов
     */
    private final ChatContextRepository chatContextRepository;

    /**
     * Создает экземпляр {@link WaitingForTodayPlaceNameStateHandler}, используя переданные аргументы
     *
     * @param weatherForecastRequestHandler обработчик запросов прогнозы погоды
     * @param botStateManager               менеджер состояний бота
     * @param chatContextRepository         репозиторий контекстов чатов
     */
    public WaitingForTodayPlaceNameStateHandler(WeatherForecastRequestHandler weatherForecastRequestHandler,
                                                BotStateManager botStateManager,
                                                ChatContextRepository chatContextRepository) {
        this.weatherForecastRequestHandler = weatherForecastRequestHandler;
        this.botStateManager = botStateManager;
        this.chatContextRepository = chatContextRepository;
    }

    @Override
    public BotMessage handle(long chatId, String text) {
        BotMessage message = new BotMessage();
        message.setText(weatherForecastRequestHandler.handleForecasts(text, ForecastTimePeriod.TODAY));
        ChatContext chatContext = chatContextRepository.findById(chatId).orElseGet(() -> {
            ChatContext newChatContext = new ChatContext();
            newChatContext.setChatId(chatId);
            return newChatContext;
        });
        chatContext.setPlaceName(null);
        chatContextRepository.save(chatContext);
        botStateManager.nextState(chatId, BotState.INITIAL);
        return message;
    }

}
