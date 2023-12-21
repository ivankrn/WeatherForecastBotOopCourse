package ru.urfu.weatherforecastbot.bot.command.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.service.ReminderService;

import java.time.format.DateTimeParseException;
import java.util.Arrays;

/**
 * Обработчик команды редактирования напоминания
 */
public class EditSubscriptionCommandHandler implements CommandHandler {

    /**
     * Сервис для управления напоминаниями
     */
    private final ReminderService reminderService;

    /**
     * Создает экземпляр {@link EditSubscriptionCommandHandler}, используя переданные аргументы
     *
     * @param reminderService сервис для управления напоминаниями
     */
    public EditSubscriptionCommandHandler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Override
    public BotMessage handle(long chatId, String userMessage) {
        BotMessage message = new BotMessage();
        String[] splittedMessage = userMessage.split(" ");
        String position = splittedMessage[1];
        String place;
        if (splittedMessage.length > 4) {
            String[] placeParts = Arrays.copyOfRange(splittedMessage, 2, splittedMessage.length - 1);
            place = String.join(" ", placeParts);
        } else {
            place = splittedMessage[2];
        }
        String time = splittedMessage[splittedMessage.length - 1];
        message.setText(handleEditSubscription(chatId, position, place, time));
        return message;
    }

    /**
     * Обработка запроса для изменения напоминания и возвращение строкового ответа
     *
     * @param chatId       ID чата
     * @param position     относительная позиция напоминания в списке
     * @param newPlaceName новое название места
     * @param newTime      новое время в виде строки (в виде стандарта UTC)
     * @return строковый ответ
     */
    private String handleEditSubscription(long chatId, String position, String newPlaceName, String newTime) {
        try {
            reminderService.editReminderByRelativePosition(
                    chatId,
                    Integer.parseInt(position),
                    newPlaceName,
                    newTime);
        } catch (NumberFormatException e) {
            return BotConstants.NOT_A_NUMBER_REMINDER_POSITION;
        } catch (DateTimeParseException e) {
            return BotConstants.WRONG_REMINDER_TIME;
        } catch (IllegalArgumentException e) {
            return BotConstants.NO_REMINDER_WITH_POSITION;
        }
        return BotConstants.EDITED_SUBSCRIPTION + " " + newTime;
    }
}
