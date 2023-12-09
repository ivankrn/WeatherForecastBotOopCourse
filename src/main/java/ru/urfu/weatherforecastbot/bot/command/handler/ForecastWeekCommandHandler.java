package ru.urfu.weatherforecastbot.bot.command.handler;

import ru.urfu.weatherforecastbot.bot.BotMessage;
import ru.urfu.weatherforecastbot.service.WeatherForecastRequestHandler;
import ru.urfu.weatherforecastbot.util.ForecastTimePeriod;

/**
 * Обработчик команды прогноза погоды на неделю
 */
public class ForecastWeekCommandHandler implements CommandHandler {

    /**
     * Обработчик запросов прогнозов погоды
     */
    private final WeatherForecastRequestHandler weatherForecastRequestHandler;

    /**
     * Создает экземляр {@link ForecastWeekCommandHandler}, используя переданные аргументы
     *
     * @param weatherForecastRequestHandler обработчик запросов прогнозов погоды
     */
    public ForecastWeekCommandHandler(WeatherForecastRequestHandler weatherForecastRequestHandler) {
        this.weatherForecastRequestHandler = weatherForecastRequestHandler;
    }

    @Override
    public BotMessage handle(long chatId, String userMessage) {
        BotMessage message = new BotMessage();
        String placeName = userMessage.substring(userMessage.indexOf(" ") + 1);
        message.setText(weatherForecastRequestHandler.handleForecasts(placeName, ForecastTimePeriod.WEEK));
        return message;
    }

}
