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
                return getPlaceNameRequestMessage();
            }
            case BotConstants.COMMAND_FORECAST_WEEK -> {
                botStateManager.nextState(chatId, BotState.WAITING_FOR_WEEK_FORECAST_PLACE_NAME);
                return getPlaceNameRequestMessage();
            }
            case BotConstants.CALLBACK_FORECAST -> {
                botStateManager.nextState(chatId, BotState.WAITING_FOR_PLACE_NAME);
                return getPlaceNameRequestMessage();
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
    private BotMessage getPlaceNameRequestMessage() {
        BotMessage message = new BotMessage();
        message.setText("Введите название места");
        message.setButtons(getCancelMenuButtons());
        return message;
    }

    /**
     * Генерирует кнопки для меню отмены
     *
     * @return кнопки для меню отмены
     */
    private List<Button> getCancelMenuButtons() {
        Button cancelButton = new Button();
        cancelButton.setText(BotConstants.CANCEL_BUTTON_TEXT);
        cancelButton.setCallback(BotConstants.COMMAND_CANCEL);
        return List.of(cancelButton);
    }

}
