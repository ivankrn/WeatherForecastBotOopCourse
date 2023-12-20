package ru.urfu.weatherforecastbot.bot.command.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.service.ReminderService;

/**
 * Обработчик команды удаления напоминания
 */
public class DeleteSubscriptionCommandHandler implements CommandHandler {

    /**
     * Сервис для управления напоминаниями
     */
    private final ReminderService reminderService;

    /**
     * Создает экземпляр {@link DeleteSubscriptionCommandHandler}, используя переданные аргументы
     *
     * @param reminderService сервис для управления напоминаниями
     */
    public DeleteSubscriptionCommandHandler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Override
    public BotMessage handle(long chatId, String userMessage) {
        BotMessage message = new BotMessage();
        String[] splittedMessage = userMessage.split(" ");
        String position = splittedMessage[1];
        message.setText(handleDeleteSubscription(chatId, position));
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
