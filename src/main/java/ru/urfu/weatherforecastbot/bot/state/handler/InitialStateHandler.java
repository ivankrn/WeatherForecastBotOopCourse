package ru.urfu.weatherforecastbot.bot.state.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.Button;
import ru.urfu.weatherforecastbot.bot.state.BotState;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;

import java.util.List;

/**
 * Обработчик начального состояния
 */
public class InitialStateHandler implements StateHandler {

    /**
     * Менеджер состояний бота
     */
    private final BotStateManager botStateManager;

    /**
     * Создает экземпляр {@link InitialStateHandler}, используя переданные аргументы
     *
     * @param botStateManager менеджер состояний бота
     */
    public InitialStateHandler(BotStateManager botStateManager) {
        this.botStateManager = botStateManager;
    }

    @Override
    public BotMessage handle(long chatId, String text) {
        BotMessage message = new BotMessage();
        String[] splittedText = text.split(" ");
        String command = splittedText[0];
        switch (command) {
            case BotConstants.COMMAND_FORECAST_TODAY -> {
                botStateManager.nextState(chatId, BotState.WAITING_FOR_TODAY_FORECAST_PLACE_NAME);
                return getForecastPlaceNameRequestMessage();
            }
            case BotConstants.COMMAND_FORECAST_WEEK -> {
                botStateManager.nextState(chatId, BotState.WAITING_FOR_WEEK_FORECAST_PLACE_NAME);
                return getForecastPlaceNameRequestMessage();
            }
            case BotConstants.CALLBACK_FORECAST -> {
                botStateManager.nextState(chatId, BotState.WAITING_FOR_PLACE_NAME);
                return getForecastPlaceNameRequestMessage();
            }
            case BotConstants.COMMAND_SUBSCRIBE -> {
                message.setText("Введите название места, для которого будут присылаться напоминания");
                message.setButtons(List.of(new Button(BotConstants.CANCEL_BUTTON_TEXT, BotConstants.COMMAND_CANCEL)));
                botStateManager.nextState(chatId, BotState.WAITING_FOR_ADD_REMINDER_PLACE_NAME);
            }
            case BotConstants.COMMAND_DEL_SUBSCRIPTION -> {
                message.setText("Введите номер напоминания, которое надо удалить");
                message.setButtons(List.of(new Button(BotConstants.CANCEL_BUTTON_TEXT, BotConstants.COMMAND_CANCEL)));
                botStateManager.nextState(chatId, BotState.WAITING_FOR_REMINDER_POSITION_TO_DELETE);
            }
            default -> message.setText(BotConstants.UNKNOWN_COMMAND);
        }
        return message;
    }

    /**
     * Возвращает сообщение, содержащее просьбу ввести название места прогноза
     *
     * @return сообщение
     */
    private BotMessage getForecastPlaceNameRequestMessage() {
        BotMessage message = new BotMessage();
        message.setText("Введите название места");
        message.setButtons(List.of(new Button(BotConstants.CANCEL_BUTTON_TEXT, BotConstants.COMMAND_CANCEL)));
        return message;
    }

}