package ru.urfu.weatherforecastbot.bot.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.bot.command.handler.CommandHandler;
import ru.urfu.weatherforecastbot.bot.state.BotStateManager;
import ru.urfu.weatherforecastbot.database.ChatContextRepository;
import ru.urfu.weatherforecastbot.service.ReminderService;
import ru.urfu.weatherforecastbot.service.WeatherForecastRequestHandler;
import ru.urfu.weatherforecastbot.util.ReminderFormatter;
import ru.urfu.weatherforecastbot.util.ReminderFormatterImpl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты контейнера команд
 */
@ExtendWith(MockitoExtension.class)
class CommandContainerTest {

    @Mock
    private WeatherForecastRequestHandler weatherForecastRequestHandler;
    @Mock
    private ChatContextRepository chatContextRepository;
    @Mock
    private BotStateManager botStateManager;
    @Mock
    private ReminderService reminderService;
    private final ReminderFormatter reminderFormatter = new ReminderFormatterImpl();

    private CommandContainer commandContainer;

    @BeforeEach
    void setUp() {
        this.commandContainer =
                new CommandContainer(weatherForecastRequestHandler, chatContextRepository,
                        botStateManager, reminderService, reminderFormatter);
    }

    @Test
    @DisplayName("Тест на добавление обработчика")
    void testAddHandler() {
        assertFalse(commandContainer.canHandle("/hello"));
        CommandHandler helloCommandHandler = (chatId, text) -> {
            BotMessage message = new BotMessage();
            message.setText("Hello!");
            return message;
        };
        commandContainer.addCommandHandler("/hello", helloCommandHandler, 0);
        assertTrue(commandContainer.canHandle("/hello"));
    }

    /**
     * Проверяет проверку возможности обработки сообщения пользователя.<br>
     * Проверки:
     * <ul>
     *     <li>если обработчика команды нет, то должен вернуть false</li>
     *     <li>если обработчик команды есть, но сообщение не содержит достаточного количества аргументов, должен
     *     вернуть false</li>
     *     <li>если обработчик команды есть и сообщение содержит достаточное количество аргументов, должен вернуть
     *     true</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест на проверку возможности обработки сообщения пользователя")
    void testCanHandle() {
        assertFalse(commandContainer.canHandle("/echo"));

        CommandHandler echoCommandHandler = (chatId, text) -> {
            BotMessage message = new BotMessage();
            message.setText(text);
            return message;
        };
        commandContainer.addCommandHandler("/echo", echoCommandHandler, 1);

        assertFalse(commandContainer.canHandle("/echo"));
        assertTrue(commandContainer.canHandle("/echo some text"));
    }

    /**
     * Проверяет нахождение обработчика команды.<br>
     * Проверки:
     * <ul>
     *     <li>если сообщение пользователя содержит команду и не содержит аргументов, должен вернуть
     *     соответствующий обработчик</li>
     *     <li>если сообщение пользователя помимо команды содержит аргументы, должен вернуть соответствующий
     *     обработчик</li>
     *     <li>если обработчик для команды не найден, должен вернуть null</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест на нахождение обработчика команды")
    void testFindCommandHandler() {
        CommandHandler helloCommandHandler = (chatId, text) -> {
            BotMessage message = new BotMessage();
            message.setText("Hello!");
            return message;
        };
        commandContainer.addCommandHandler("/hello", helloCommandHandler, 0);

        assertEquals(helloCommandHandler, commandContainer.findCommandHandler("/hello"));

        CommandHandler echoCommandHandler = (chatId, text) -> {
            BotMessage message = new BotMessage();
            message.setText(text);
            return message;
        };
        commandContainer.addCommandHandler("/echo", echoCommandHandler, 1);
        assertEquals(echoCommandHandler, commandContainer.findCommandHandler("/echo some text"));
        assertNull(commandContainer.findCommandHandler("/abc"));
    }
}