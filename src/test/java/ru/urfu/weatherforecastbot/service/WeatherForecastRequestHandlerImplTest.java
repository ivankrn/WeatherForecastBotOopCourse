package ru.urfu.weatherforecastbot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.urfu.weatherforecastbot.model.Place;
import ru.urfu.weatherforecastbot.model.WeatherForecast;
import ru.urfu.weatherforecastbot.util.ForecastTimePeriod;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatter;
import ru.urfu.weatherforecastbot.util.WeatherForecastFormatterImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Тесты обработчика запросов на получение прогнозов погоды
 */
@ExtendWith(MockitoExtension.class)
class WeatherForecastRequestHandlerImplTest {

    /**
     * Сервис для получения прогнозов погоды
     */
    private final WeatherForecastService weatherService;
    /**
     * Форматировщик прогноза погоды в удобочитаемый вид
     */
    private final WeatherForecastFormatter forecastFormatter = new WeatherForecastFormatterImpl();
    /**
     * Обработчик запросов на получение прогнозов погоды
     */
    private final WeatherForecastRequestHandler weatherForecastRequestHandler;

    public WeatherForecastRequestHandlerImplTest(@Mock WeatherForecastService weatherService) {
        this.weatherService = weatherService;
        weatherForecastRequestHandler = new WeatherForecastRequestHandlerImpl(weatherService, forecastFormatter);
    }

    /**
     * Проверяет обработку запроса прогноза погоды на сегодня.<br>
     * Проверки:
     * <ul>
     *     <li>если место найдено, должен вернуть отформатированный прогноз погоды на сегодня</li>
     *     <li>если место не найдено, должен вернуть сообщение, что место не найдено</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест на обработку запроса прогноза погоды на сегодня")
    void testTodayForecastsHandle() {
        LocalDateTime today = LocalDateTime.of(2023, 10, 10, 0, 0);
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        when(weatherService.getForecast("Екатеринбург", 1)).thenReturn(todayForecast);

        assertEquals("""
                        🌡️ Прогноз погоды на сегодня (Екатеринбург):
                                                
                        00-00: 0.0°C (по ощущению 0.0°C)
                        01-00: 0.0°C (по ощущению 0.0°C)
                        02-00: 0.0°C (по ощущению 0.0°C)
                        03-00: 0.0°C (по ощущению 0.0°C)
                        04-00: 0.0°C (по ощущению 0.0°C)
                        05-00: 0.0°C (по ощущению 0.0°C)
                        06-00: 0.0°C (по ощущению 0.0°C)
                        07-00: 0.0°C (по ощущению 0.0°C)
                        08-00: 0.0°C (по ощущению 0.0°C)
                        09-00: 0.0°C (по ощущению 0.0°C)
                        10-00: 0.0°C (по ощущению 0.0°C)
                        11-00: 0.0°C (по ощущению 0.0°C)
                        12-00: 0.0°C (по ощущению 0.0°C)
                        13-00: 0.0°C (по ощущению 0.0°C)
                        14-00: 0.0°C (по ощущению 0.0°C)
                        15-00: 0.0°C (по ощущению 0.0°C)
                        16-00: 0.0°C (по ощущению 0.0°C)
                        17-00: 0.0°C (по ощущению 0.0°C)
                        18-00: 0.0°C (по ощущению 0.0°C)
                        19-00: 0.0°C (по ощущению 0.0°C)
                        20-00: 0.0°C (по ощущению 0.0°C)
                        21-00: 0.0°C (по ощущению 0.0°C)
                        22-00: 0.0°C (по ощущению 0.0°C)
                        23-00: 0.0°C (по ощущению 0.0°C)""",
                weatherForecastRequestHandler.handleForecasts("Екатеринбург", ForecastTimePeriod.TODAY));

        when(weatherService.getForecast("random-text", 1)).thenReturn(List.of());

        assertEquals("Извините, данное место не найдено.",
                weatherForecastRequestHandler.handleForecasts("random-text", ForecastTimePeriod.TODAY));
    }

    /**
     * Проверяет обработку запроса прогноза погоды на завтра.<br>
     * Проверки:
     * <ul>
     *     <li>если место найдено, должен вернуть отформатированный прогноз погоды на завтра</li>
     *     <li>если место не найдено, должен вернуть сообщение, что место не найдено</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест на обработку запроса прогноза погоды на завтра")
    void testTomorrowForecastsHandle() {
        LocalDateTime today = LocalDateTime.of(2023, 10, 10, 0, 0);
        int days = 2;
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int day = 0; day < days; day++) {
            for (int hour = 0; hour < hours; hour++) {
                todayForecast.add(
                        new WeatherForecast(place, today.withHour(hour).plusDays(day), 0, 0));
            }
        }
        when(weatherService.getForecast("Екатеринбург", 2)).thenReturn(todayForecast);

        assertEquals("""
                        🌡️ Прогноз погоды на завтра (Екатеринбург):
                                                
                        00-00: 0.0°C (по ощущению 0.0°C)
                        01-00: 0.0°C (по ощущению 0.0°C)
                        02-00: 0.0°C (по ощущению 0.0°C)
                        03-00: 0.0°C (по ощущению 0.0°C)
                        04-00: 0.0°C (по ощущению 0.0°C)
                        05-00: 0.0°C (по ощущению 0.0°C)
                        06-00: 0.0°C (по ощущению 0.0°C)
                        07-00: 0.0°C (по ощущению 0.0°C)
                        08-00: 0.0°C (по ощущению 0.0°C)
                        09-00: 0.0°C (по ощущению 0.0°C)
                        10-00: 0.0°C (по ощущению 0.0°C)
                        11-00: 0.0°C (по ощущению 0.0°C)
                        12-00: 0.0°C (по ощущению 0.0°C)
                        13-00: 0.0°C (по ощущению 0.0°C)
                        14-00: 0.0°C (по ощущению 0.0°C)
                        15-00: 0.0°C (по ощущению 0.0°C)
                        16-00: 0.0°C (по ощущению 0.0°C)
                        17-00: 0.0°C (по ощущению 0.0°C)
                        18-00: 0.0°C (по ощущению 0.0°C)
                        19-00: 0.0°C (по ощущению 0.0°C)
                        20-00: 0.0°C (по ощущению 0.0°C)
                        21-00: 0.0°C (по ощущению 0.0°C)
                        22-00: 0.0°C (по ощущению 0.0°C)
                        23-00: 0.0°C (по ощущению 0.0°C)""",
                weatherForecastRequestHandler.handleForecasts("Екатеринбург", ForecastTimePeriod.TOMORROW));

        when(weatherService.getForecast("random-text", 2)).thenReturn(List.of());

        assertEquals("Извините, данное место не найдено.",
                weatherForecastRequestHandler.handleForecasts("random-text", ForecastTimePeriod.TOMORROW));
    }

    /**
     * Проверяет обработку запроса прогноза погоды на неделю.<br>
     * Проверки:
     * <ul>
     *     <li>если место найдено, должен вернуть отформатированный прогноз погоды на неделю</li>
     *     <li>если место не найдено, должен вернуть сообщение, что место не найдено</li>
     * </ul>
     */
    @Test
    @DisplayName("Тест на обработку запроса прогноза погоды на неделю")
    void testWeekForecastsHandle() {
        LocalDateTime now = LocalDateTime.of(2023, 10, 10, 0, 0);
        int days = 7;
        int hourInterval = 4;
        List<WeatherForecast> weekForecast = new ArrayList<>();
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int day = 0; day < days; day++) {
            for (int hour = 0; hour < 24; hour += hourInterval) {
                weekForecast.add(
                        new WeatherForecast(place, now.plusDays(day).withHour(hour), 0, 0));
            }
        }
        when(weatherService.getForecast("Екатеринбург", 7)).thenReturn(weekForecast);

        assertEquals("""
                        🌡️ Прогноз погоды на неделю (Екатеринбург):
                                                
                        10.10.2023:
                        00-00: 0.0°C (по ощущению 0.0°C)
                        04-00: 0.0°C (по ощущению 0.0°C)
                        08-00: 0.0°C (по ощущению 0.0°C)
                        12-00: 0.0°C (по ощущению 0.0°C)
                        16-00: 0.0°C (по ощущению 0.0°C)
                        20-00: 0.0°C (по ощущению 0.0°C)
                                                
                        11.10.2023:
                        00-00: 0.0°C (по ощущению 0.0°C)
                        04-00: 0.0°C (по ощущению 0.0°C)
                        08-00: 0.0°C (по ощущению 0.0°C)
                        12-00: 0.0°C (по ощущению 0.0°C)
                        16-00: 0.0°C (по ощущению 0.0°C)
                        20-00: 0.0°C (по ощущению 0.0°C)
                                                
                        12.10.2023:
                        00-00: 0.0°C (по ощущению 0.0°C)
                        04-00: 0.0°C (по ощущению 0.0°C)
                        08-00: 0.0°C (по ощущению 0.0°C)
                        12-00: 0.0°C (по ощущению 0.0°C)
                        16-00: 0.0°C (по ощущению 0.0°C)
                        20-00: 0.0°C (по ощущению 0.0°C)
                                                
                        13.10.2023:
                        00-00: 0.0°C (по ощущению 0.0°C)
                        04-00: 0.0°C (по ощущению 0.0°C)
                        08-00: 0.0°C (по ощущению 0.0°C)
                        12-00: 0.0°C (по ощущению 0.0°C)
                        16-00: 0.0°C (по ощущению 0.0°C)
                        20-00: 0.0°C (по ощущению 0.0°C)
                                                
                        14.10.2023:
                        00-00: 0.0°C (по ощущению 0.0°C)
                        04-00: 0.0°C (по ощущению 0.0°C)
                        08-00: 0.0°C (по ощущению 0.0°C)
                        12-00: 0.0°C (по ощущению 0.0°C)
                        16-00: 0.0°C (по ощущению 0.0°C)
                        20-00: 0.0°C (по ощущению 0.0°C)
                                                
                        15.10.2023:
                        00-00: 0.0°C (по ощущению 0.0°C)
                        04-00: 0.0°C (по ощущению 0.0°C)
                        08-00: 0.0°C (по ощущению 0.0°C)
                        12-00: 0.0°C (по ощущению 0.0°C)
                        16-00: 0.0°C (по ощущению 0.0°C)
                        20-00: 0.0°C (по ощущению 0.0°C)
                                                
                        16.10.2023:
                        00-00: 0.0°C (по ощущению 0.0°C)
                        04-00: 0.0°C (по ощущению 0.0°C)
                        08-00: 0.0°C (по ощущению 0.0°C)
                        12-00: 0.0°C (по ощущению 0.0°C)
                        16-00: 0.0°C (по ощущению 0.0°C)
                        20-00: 0.0°C (по ощущению 0.0°C)""",
                weatherForecastRequestHandler.handleForecasts("Екатеринбург", ForecastTimePeriod.WEEK));

        when(weatherService.getForecast("random-text", 7)).thenReturn(List.of());

        assertEquals("Извините, данное место не найдено.",
                weatherForecastRequestHandler.handleForecasts("random-text", ForecastTimePeriod.WEEK));
    }
}