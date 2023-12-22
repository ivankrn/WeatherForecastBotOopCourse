package ru.urfu.weatherforecastbot.bot.state.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.state.BotState;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.model.ChatContext;
import ru.urfu.weatherforecastbot.service.ReminderService;

import java.time.format.DateTimeParseException;

/**
 * Обработчик состояния ожидания времени для добавления напоминания
 */
public class WaitingForAddReminderTimeStateHandler implements StateHandler {

    /**
     * Сервис для управления напоминаниями
     */
    private final ReminderService reminderService;
    /**
     * Менеджер состояний бота
     */
    private final BotStateManager botStateManager;
    /**
     * Репозиторий контекстов чатов
     */
    private final ChatContextRepository chatContextRepository;

    /**
     * Создает экземпляр {@link WaitingForAddReminderTimeStateHandler}, используя переданные аргументы
     *
     * @param reminderService       сервис для управления напоминаниями
     * @param botStateManager       менеджер состояний бота
     * @param chatContextRepository репозиторий контекстов чатов
     */
    public WaitingForAddReminderTimeStateHandler(ReminderService reminderService, BotStateManager botStateManager,
                                                 ChatContextRepository chatContextRepository) {
        this.reminderService = reminderService;
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
        try {
            reminderService.addReminder(chatId, chatContext.getPlaceName(), text);
            message.setText(BotConstants.ADDED_SUBSCRIPTION + " " + text);
            chatContext.setPlaceName(null);
            chatContextRepository.save(chatContext);
            botStateManager.nextState(chatId, BotState.INITIAL);
        } catch (DateTimeParseException e) {
            message.setText(BotConstants.WRONG_REMINDER_TIME);
        }
        return message;
    }

}
