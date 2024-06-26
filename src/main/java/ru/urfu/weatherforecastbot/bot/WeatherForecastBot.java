package ru.urfu.weatherforecastbot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.urfu.weatherforecastbot.config.BotConfig;

import java.util.List;

/**
 * Бот для получения прогноза погоды
 */
@Component
public class WeatherForecastBot extends TelegramLongPollingBot implements Bot {

    /**
     * Конфигурация бота
     */
    private final BotConfig botConfig;
    /**
     * Обработчик сообщений
     */
    private final MessageHandler messageHandler;
    /**
     * Логгер
     */
    private final Logger logger = LoggerFactory.getLogger(WeatherForecastBot.class);

    /**
     * Создает экземпляр WeatherForecastBot, используя переданные аргументы
     *
     * @param botConfig      конфигурация бота
     * @param messageHandler обработчик сообщений
     */
    @Autowired
    public WeatherForecastBot(BotConfig botConfig, @Lazy MessageHandler messageHandler) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.messageHandler = messageHandler;
    }

    /**
     * Обработчик событий Telegram
     *
     * @param update событие
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            BotMessage responseMessage = messageHandler.handle(chatId, message.getText());
            sendMessage(chatId, responseMessage);
        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String text = update.getCallbackQuery().getData();
            BotMessage responseMessage = messageHandler.handle(chatId, text);
            sendMessage(chatId, responseMessage);
        }
    }

    /**
     * Возвращает название бота
     *
     * @return название бота
     */
    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public void sendMessage(long chatId, BotMessage message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message.getText());
        sendMessage.setReplyMarkup(convertToTelegramButtons(message.getButtons()));
        executeMessageWithLogging(sendMessage);
    }

    /**
     * Выполняет сообщение Telegram с логированием
     *
     * @param message сообщение
     */
    private void executeMessageWithLogging(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Инициализирует бота при запуске приложения
     */
    @EventListener({ContextRefreshedEvent.class})
    private void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Преобразует {@link Button кнопки} в {@link InlineKeyboardButton Telegram кнопки}
     *
     * @param buttons кнопки
     * @return Telegram кнопки
     */
    private InlineKeyboardMarkup convertToTelegramButtons(List<Button> buttons) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> telegramButtons = buttons.stream().map(button -> {
                    InlineKeyboardButton telegramButton = new InlineKeyboardButton();
                    telegramButton.setText(button.getText());
                    telegramButton.setCallbackData(button.getCallback());
                    return telegramButton;
                })
                .toList();
        markup.setKeyboard(List.of(telegramButtons));
        return markup;
    }
}
