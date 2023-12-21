package ru.urfu.weatherforecastbot.bot.state.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.state.BotState;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;
import ru.urfu.weatherforecastbot.service.ReminderService;

/**
 * Обработчик состояния ожидания позиции для удаления напоминания
 */
public class WaitingForReminderPositionToDeleteStateHandler implements StateHandler {

    /**
     * Сервис для управления напоминаниями
     */
    private final ReminderService reminderService;
    /**
     * Менеджер состояний бота
     */
    private final BotStateManager botStateManager;

    /**
     * Создает экземпляр {@link WaitingForReminderPositionToDeleteStateHandler}, используя переданные аргументы
     *
     * @param reminderService сервис для управления напоминаниями
     * @param botStateManager менеджер состояний бота
     */
    public WaitingForReminderPositionToDeleteStateHandler(ReminderService reminderService,
                                                          BotStateManager botStateManager) {
        this.reminderService = reminderService;
        this.botStateManager = botStateManager;
    }

    @Override
    public BotMessage handle(long chatId, String text) {
        BotMessage message = new BotMessage();
        message.setText(handleDeleteSubscription(chatId, text));
        botStateManager.nextState(chatId, BotState.INITIAL);
        return message;
    }

    /**
     * Обрабатывает запрос на удаление напоминания и возвращает ответ в виде строки
     *
     * @param chatId   ID чата
     * @param position относительная позиция напоминания в списке
     * @return ответ в виде строки
     */
    private String handleDeleteSubscription(long chatId, String position) {
        try {
            int positionAsNumber = Integer.parseInt(position);
            reminderService.deleteReminderByRelativePosition(chatId, positionAsNumber);
            return BotConstants.DELETED_SUBSCRIPTION;
        } catch (NumberFormatException e) {
            return BotConstants.NOT_A_NUMBER_REMINDER_POSITION;
        } catch (IllegalArgumentException e) {
            return BotConstants.NO_REMINDER_WITH_POSITION;
        }
    }

}
