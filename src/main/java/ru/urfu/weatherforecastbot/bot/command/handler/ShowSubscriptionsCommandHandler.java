package ru.urfu.weatherforecastbot.bot.command.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.model.Reminder;
import ru.urfu.weatherforecastbot.service.ReminderService;
import ru.urfu.weatherforecastbot.util.ReminderFormatter;

import java.util.List;

/**
 * Обработчик команды просмотра списка подписок
 */
public class ShowSubscriptionsCommandHandler implements CommandHandler {

    /**
     * Сервис для управления напоминаниями
     */
    private final ReminderService reminderService;
    /**
     * Форматировщик напоминаний
     */
    private final ReminderFormatter reminderFormatter;

    /**
     * Создает экземпляр {@link ShowSubscriptionsCommandHandler}, используя переданные аргументы
     *
     * @param reminderService   сервис для управления напоминаниями
     * @param reminderFormatter форматировщик напоминаний
     */
    public ShowSubscriptionsCommandHandler(ReminderService reminderService, ReminderFormatter reminderFormatter) {
        this.reminderService = reminderService;
        this.reminderFormatter = reminderFormatter;
    }

    @Override
    public BotMessage handle(long chatId, String userMessage) {
        BotMessage message = new BotMessage();
        message.setText(handleShowSubscriptions(chatId));
        return message;
    }

    /**
     * Обработка запроса для отображения списка напоминаний и возвращение строкового ответа
     *
     * @param chatId ID чата
     * @return строковый ответ
     */
    private String handleShowSubscriptions(long chatId) {
        List<Reminder> reminders = reminderService.findAllForChatId(chatId);
        if (reminders.isEmpty()) {
            return BotConstants.NO_REMINDERS;
        }
        return reminderFormatter.formatReminders(reminders);
    }
}
