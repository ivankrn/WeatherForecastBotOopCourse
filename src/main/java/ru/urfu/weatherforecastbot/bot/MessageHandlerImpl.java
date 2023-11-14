package ru.urfu.weatherforecastbot.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.service.WeatherForecastService;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;

import java.util.List;

@Component
public class MessageHandlerImpl implements MessageHandler {

    /**
     * Сервис для получения прогнозов погоды
     */
    private final WeatherForecastService weatherService;
    /**
     * Форматировщик прогноза погоды в удобочитаемый вид
     */
    private final WeatherForecastFormatter forecastFormatter;
    /**
     * Флаг, указываюший на то, что бот запущен
     */
    private boolean botStarted = false;

    public MessageHandlerImpl(WeatherForecastService weatherService,
                              WeatherForecastFormatter forecastFormatter) {
        this.weatherService = weatherService;
        this.forecastFormatter = forecastFormatter;
    }

    @Override
    public SendMessage handle(Message message) {
        long chatId = message.getChatId();
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);
        if (message.hasText()) {
            String[] receivedText = message.getText().split(" ");
            String command = receivedText[0];

            if ("/start".equals(command)) {
                botStarted = true;
                responseMessage.setText(startBot());
            } else if (!botStarted) {
                responseMessage.setText("Пожалуйста, используйте /start для запуска бота.");
            } else {
                switch (command) {
                    case "/info" -> {
                        if (receivedText.length < 2) {
                            responseMessage.setText(BotText.WRONG_COMMAND_SYNTAX.text);
                        } else {
                            String place = receivedText[1];
                            responseMessage.setText(handleTodayForecasts(place));
                        }
                    }
                    case "/info_week" -> {
                        if (receivedText.length < 2) {
                            responseMessage.setText(BotText.WRONG_COMMAND_SYNTAX.text);
                        } else {
                            String place = receivedText[1];
                            responseMessage.setText(handleWeekForecasts(place));
                        }
                    }
                    case "/help" -> responseMessage.setText(getHelpMessage());
                    default -> responseMessage.setText(BotText.UNKNOWN_COMMAND.text);
                }
            }
        }
        
        return responseMessage;
    }

    /**
     * Обрабатывает запрос на получение прогноза погоды по часам на сегодня и возвращает ответ в виде строки
     *
     * @param placeName название места
     * @return ответ в виде строки
     */
    private String handleTodayForecasts(String placeName) {
        List<WeatherForecast> todayForecasts = weatherService.getForecast(placeName, 1);
        if (todayForecasts == null) {
            return BotText.NOT_FOUND.text;
        }
        return forecastFormatter.formatTodayForecast(todayForecasts);
    }

    /**
     * Обрабатывает запрос на получение прогноза погоды на каждые 4 часа этой недели и возвращает ответ в виде строки
     *
     * @param placeName название места
     * @return ответ в виде строки
     */
    private String handleWeekForecasts(String placeName) {
        List<WeatherForecast> todayForecasts = weatherService.getForecast(placeName, 7);
        if (todayForecasts == null) {
            return BotText.NOT_FOUND.text;
        }
        return forecastFormatter.formatWeekForecast(todayForecasts);
    }

    /**
     * Возвращает сводку команд, доступную пользователю
     *
     * @return существующие команды
     */
    private String getHelpMessage() {
        return """
                Вы зашли в меню помощи. Для вас доступны следующие команды:
                /start - запустить бота
                /help - меню помощи
                /info <название населенного пункта> - вывести прогноз погоды для <населенного пункта>
                /info_week <название населенного пункта> - вывести прогноз погоды для <название населенного пункта> на неделю вперёд""";
    }

    private String startBot() {
        return """
               Здравствуйте! Я бот для просмотра прогноза погоды. Доступны следующие команды: \s
               /start_ - запустить бота
               /help_ - меню помощи
               /info <название населенного пункта>_ - вывести прогноз погоды для <населенного пункта>
               /info_week <название населенного пункта>_ - вывести прогноз погоды для <название населенного пункта> на неделю вперёд
               """;
    }
}
