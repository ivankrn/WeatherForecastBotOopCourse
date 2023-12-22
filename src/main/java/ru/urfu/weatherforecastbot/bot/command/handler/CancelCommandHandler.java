package ru.urfu.weatherforecastbot.bot.command.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.Button;
import ru.urfu.weatherforecastbot.bot.state.BotState;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.model.ChatContext;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды отмены
 */
public class CancelCommandHandler implements CommandHandler {

    /**
     * Репозиторий контекстов чатов
     */
    private final ChatContextRepository chatContextRepository;
    /**
     * Менеджер состояний бота
     */
    private final BotStateManager botStateManager;

    /**
     * Создает экземпляр {@link CancelCommandHandler}, используя переданные аргументы
     *
     * @param chatContextRepository репозиторий контекстов чатов
     * @param botStateManager       менеджер состояний бота
     */
    public CancelCommandHandler(ChatContextRepository chatContextRepository, BotStateManager botStateManager) {
        this.chatContextRepository = chatContextRepository;
        this.botStateManager = botStateManager;
    }

    @Override
    public BotMessage handle(long chatId, String userMessage) {
        BotMessage message = new BotMessage();
        message.setText("Вы вернулись в основное меню");
        message.setButtons(getMainMenuButtons());
        Optional<ChatContext> chatContext = chatContextRepository.findById(chatId);
        if (chatContext.isPresent()) {
            ChatContext newChatContext = chatContext.get();
            newChatContext.setPlaceName(null);
            chatContextRepository.save(newChatContext);
        }
        botStateManager.nextState(chatId, BotState.INITIAL);
        return message;
    }

    /**
     * Генерирует кнопки для основного меню
     *
     * @return кнопки для основного меню
     */
    private List<Button> getMainMenuButtons() {
        Button forecastButton = new Button(BotConstants.FORECAST_BUTTON_TEXT, BotConstants.CALLBACK_FORECAST);
        Button helpButton = new Button(BotConstants.HELP_BUTTON_TEXT, BotConstants.COMMAND_HELP);
        Button cancelButton = new Button(BotConstants.CANCEL_BUTTON_TEXT, BotConstants.COMMAND_CANCEL);
        return List.of(forecastButton, helpButton, cancelButton);
    }

}
