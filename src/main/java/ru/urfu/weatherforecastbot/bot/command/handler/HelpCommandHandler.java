package ru.urfu.weatherforecastbot.bot.command.handler;

import ru.urfu.weatherforecastbot.bot.BotConstants;
import ru.urfu.weatherforecastbot.bot.BotMessage;

/**
 * Обработчик команды помощи
 */
public class HelpCommandHandler implements CommandHandler {

    @Override
    public BotMessage handle(long chatId, String userMessage) {
        BotMessage message = new BotMessage();
        message.setText(BotConstants.HELP_TEXT);
        return message;
    }

}
