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
 * Обработчик состояния ожидания времени для редактирования напоминания
 */
public class WaitingForEditReminderTimeStateHandler implements StateHandler {

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
     * Создает экземпляр {@link WaitingForEditReminderTimeStateHandler}, используя переданные аргументы
     *
     * @param reminderService       сервис для управления напоминаниями
     * @param botStateManager       менеджер состояний бота
     * @param chatContextRepository репозиторий контекстов чатов
     */
    public WaitingForEditReminderTimeStateHandler(ReminderService reminderService, BotStateManager botStateManager,
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
            reminderService.editReminderByRelativePosition(
                    chatId,
                    chatContext.getReminderPosition(),
                    chatContext.getPlaceName(),
                    text);
            message.setText(BotConstants.EDITED_SUBSCRIPTION + " " + text);
            botStateManager.nextState(chatId, BotState.INITIAL);
        } catch (DateTimeParseException e) {
            message.setText(BotConstants.WRONG_REMINDER_TIME);
        } catch (IllegalArgumentException e) {
            message.setText(BotConstants.NO_REMINDER_WITH_POSITION);
            botStateManager.nextState(chatId, BotState.INITIAL);
        }
        return message;
    }

}
