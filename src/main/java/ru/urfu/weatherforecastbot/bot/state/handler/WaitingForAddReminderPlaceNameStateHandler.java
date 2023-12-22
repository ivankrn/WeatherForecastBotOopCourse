package ru.urfu.weatherforecastbot.bot.state.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.Button;
import ru.urfu.weatherforecastbot.bot.state.BotState;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.model.ChatContext;

import java.util.List;

/**
 * Обработчик состояния ожидания места для добавления напоминания
 */
public class WaitingForAddReminderPlaceNameStateHandler implements StateHandler {

    /**
     * Менеджер состояний бота
     */
    private final BotStateManager botStateManager;
    /**
     * Репозиторий контекстов чатов
     */
    private final ChatContextRepository chatContextRepository;

    /**
     * Создает экземпляр {@link WaitingForAddReminderPlaceNameStateHandler}, используя переданные аргументы
     *
     * @param botStateManager       менеджер состояний бота
     * @param chatContextRepository репозиторий контекстов чатов
     */
    public WaitingForAddReminderPlaceNameStateHandler(BotStateManager botStateManager,
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
        message.setText("Введите время (в UTC), когда должно присылаться " +
                "напоминание прогноза (пример: 08:00)");
        message.setButtons(List.of(new Button(BotConstants.CANCEL_BUTTON_TEXT, BotConstants.COMMAND_CANCEL)));
        botStateManager.nextState(chatId, BotState.WAITING_FOR_ADD_REMINDER_TIME);
        return message;
    }

}
