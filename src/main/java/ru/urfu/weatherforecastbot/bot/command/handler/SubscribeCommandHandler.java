package ru.urfu.weatherforecastbot.bot.command.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.service.ReminderService;

import java.time.format.DateTimeParseException;
import java.util.Arrays;

/**
 * Обработчик команды создания подписки на напоминания прогнозов погоды
 */
public class SubscribeCommandHandler implements CommandHandler {

    /**
     * Сервис для управления напоминаниями
     */
    private final ReminderService reminderService;

    /**
     * Создает экземпляр {@link SubscribeCommandHandler}, используя переданные аргументы
     *
     * @param reminderService сервис для управления напоминаниями
     */
    public SubscribeCommandHandler(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Override
    public BotMessage handle(long chatId, String userMessage) {
        BotMessage message = new BotMessage();
        String[] splittedMessage = userMessage.split(" ");
        String place;
        if (splittedMessage.length > 3) {
            String[] placeParts = Arrays.copyOfRange(splittedMessage, 1, splittedMessage.length - 1);
            place = String.join(" ", placeParts);
        } else {
            place = splittedMessage[1];
        }
        String time = splittedMessage[splittedMessage.length - 1];
        message.setText(handleNewSubscription(chatId, place, time));
        return message;
    }

    /**
     * Обрабатывает запрос на добавление напоминания и возвращает ответ в виде строки
     *
     * @param chatId    ID чата
     * @param placeName название места
     * @param time      время в виде строки (в UTC)
     * @return ответ в виде строки
     */
    private String handleNewSubscription(long chatId, String placeName, String time) {
        try {
            reminderService.addReminder(chatId, placeName, time);
        } catch (DateTimeParseException e) {
            return BotConstants.WRONG_REMINDER_TIME;
        }
        return BotConstants.ADDED_SUBSCRIPTION + " " + time;
    }
}
