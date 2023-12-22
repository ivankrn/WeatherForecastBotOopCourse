package ru.urfu.weatherforecastbot.bot.state.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.Button;
import ru.urfu.weatherforecastbot.bot.state.BotState;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.model.ChatContext;
import ru.urfu.weatherforecastbot.service.WeatherForecastRequestHandler;
import ru.urfu.weatherforecastbot.util.ForecastTimePeriod;

import java.util.List;

/**
 * Обработчик состояния ожидания временного периода для прогноза погоды
 */
public class WaitingForTimePeriodStateHandler implements StateHandler {

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
     * Создает экземпляр {@link WaitingForTimePeriodStateHandler}, используя переданные аргументы
     *
     * @param weatherForecastRequestHandler обработчик запросов прогнозы погоды
     * @param botStateManager               менеджер состояний бота
     * @param chatContextRepository         репозиторий контекстов чатов
     */
    public WaitingForTimePeriodStateHandler(WeatherForecastRequestHandler weatherForecastRequestHandler,
                                            BotStateManager botStateManager,
                                            ChatContextRepository chatContextRepository) {
        this.weatherForecastRequestHandler = weatherForecastRequestHandler;
        this.botStateManager = botStateManager;
        this.chatContextRepository = chatContextRepository;
    }

    @Override
    public BotMessage handle(long chatId, String text) {
        BotMessage message = new BotMessage();
        ChatContext chatContext = chatContextRepository.findById(chatId).orElseGet(() -> {
            ChatContext newChatContext = new ChatContext();
            newChatContext.setChatId(chatId);
            return newChatContext;
        });
        if (text.equals(ForecastTimePeriod.TODAY.getText())) {
            message.setText(
                    weatherForecastRequestHandler.handleForecasts(
                            chatContext.getPlaceName(), ForecastTimePeriod.TODAY));
            botStateManager.nextState(chatId, BotState.INITIAL);
        } else if (text.equals(ForecastTimePeriod.TOMORROW.getText())) {
            message.setText(
                    weatherForecastRequestHandler.handleForecasts(
                            chatContext.getPlaceName(), ForecastTimePeriod.TOMORROW));
            botStateManager.nextState(chatId, BotState.INITIAL);
        } else if (text.equals(ForecastTimePeriod.WEEK.getText())) {
            message.setText(
                    weatherForecastRequestHandler.handleForecasts(
                            chatContext.getPlaceName(), ForecastTimePeriod.WEEK));
            botStateManager.nextState(chatId, BotState.INITIAL);
        } else {
            message.setText("Введите корректный временной период. Допустимые значения: сегодня, завтра, неделя");
            message.setButtons(getTimePeriodMenuButtons());
            return message;
        }
        chatContext.setPlaceName(null);
        chatContextRepository.save(chatContext);
        return message;
    }

    /**
     * Генерирует кнопки для меню периода времени
     *
     * @return кнопки для меню периода времени
     */
    private List<Button> getTimePeriodMenuButtons() {
        Button todayButton = new Button(ForecastTimePeriod.TODAY.getText(), ForecastTimePeriod.TODAY.getText());
        Button tomorrowButton = new Button(ForecastTimePeriod.TOMORROW.getText(), ForecastTimePeriod.TOMORROW.getText());
        Button weekButton = new Button(ForecastTimePeriod.WEEK.getText(), ForecastTimePeriod.WEEK.getText());
        Button cancelButton = new Button(BotConstants.CANCEL_BUTTON_TEXT, BotConstants.COMMAND_CANCEL);
        return List.of(todayButton, tomorrowButton, weekButton, cancelButton);
    }

}
