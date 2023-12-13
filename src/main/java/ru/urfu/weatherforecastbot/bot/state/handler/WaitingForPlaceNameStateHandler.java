package ru.urfu.weatherforecastbot.bot.state.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.Button;
import ru.urfu.weatherforecastbot.bot.state.BotState;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.model.ChatContext;
import ru.urfu.weatherforecastbot.util.ForecastTimePeriod;

import java.util.List;

/**
 * Обработчик состояния ожидания места
 */
public class WaitingForPlaceNameStateHandler implements StateHandler {

    /**
     * Менеджер состояний бота
     */
    private final BotStateManager botStateManager;
    /**
     * Репозиторий контекстов чатов
     */
    private final ChatContextRepository chatContextRepository;

    /**
     * Создает экземпляр {@link WaitingForPlaceNameStateHandler}, используя переданные аргументы
     *
     * @param botStateManager       менеджер состояний бота
     * @param chatContextRepository репозиторий контекстов чатов
     */
    public WaitingForPlaceNameStateHandler(BotStateManager botStateManager,
                                           ChatContextRepository chatContextRepository) {
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
        chatContext.setPlaceName(text);
        chatContextRepository.save(chatContext);
        message.setText("Выберите временной период для просмотра (сегодня, завтра, неделя)");
        message.setButtons(getTimePeriodMenuButtons());
        botStateManager.nextState(chatId, BotState.WAITING_FOR_TIME_PERIOD);
        return message;
    }

    /**
     * Генерирует кнопки для меню периода времени
     *
     * @return кнопки для меню периода времени
     */
    private List<Button> getTimePeriodMenuButtons() {
        Button todayButton = new Button();
        todayButton.setText(ForecastTimePeriod.TODAY.getText());
        todayButton.setCallback(ForecastTimePeriod.TODAY.getText());
        Button tomorrowButton = new Button();
        tomorrowButton.setText(ForecastTimePeriod.TOMORROW.getText());
        tomorrowButton.setCallback(ForecastTimePeriod.TOMORROW.getText());
        Button weekButton = new Button();
        weekButton.setText(ForecastTimePeriod.WEEK.getText());
        weekButton.setCallback(ForecastTimePeriod.WEEK.getText());
        Button cancelButton = new Button();
        cancelButton.setText(BotConstants.CANCEL_BUTTON_TEXT);
        cancelButton.setCallback(BotConstants.COMMAND_CANCEL);
        return List.of(todayButton, tomorrowButton, weekButton, cancelButton);
    }
}
