package ru.urfu.weatherforecastbot.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тесты форматировщика прогнозов погоды
 */
class WeatherForecastFormatterImplTest {

    /**
     * Форматировщик прогноза погоды в удобочитаемый вид
     */
    private final WeatherForecastFormatter formatter = new WeatherForecastFormatterImpl();

    @Test
    @DisplayName("При непустом прогнозе погоды на сегодня должен возвращать отформатированный прогноз погоды")
    void givenTodayNotEmptyForecast_whenFormatTodayForecast_thenReturnFormattedTodayForecast() {
        LocalDateTime today = LocalDateTime.of(2023, 11, 5, 0, 0);
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(today.withHour(hour), 0, 0));
        }
        String expected = """
                \uD83C\uDF21️ Прогноз погоды на сегодня:

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
                23-00: 0.0°C (по ощущению 0.0°C)""";

        String actual = formatter.formatTodayForecast(todayForecast);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("При пустом прогнозе погоды на сегодня должен возвращать только заголовок прогноза погоды")
    void givenTodayEmptyForecast_whenFormatTodayForecast_thenReturnOnlyHeader() {
        List<WeatherForecast> todayForecast = List.of();
        String expected = "\uD83C\uDF21️ Прогноз погоды на сегодня:\n\n";

        String actual = formatter.formatTodayForecast(todayForecast);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("При непустом прогнозе погоды на неделю вперед должен возвращать отформатированный прогноз погоды")
    void givenWeekNotEmptyForecast_whenFormatWeekForecast_thenReturnFormattedWeekForecast() {
        LocalDateTime today = LocalDateTime.of(2023, 11, 12, 0, 0);
        int days = 7;
        int hourInterval = 4;
        List<WeatherForecast> weekForecast = new ArrayList<>();
        for (int day = 0; day < days; day++) {
            for (int hour = 0; hour < 24; hour += hourInterval) {
                weekForecast.add(
                        new WeatherForecast(today.plusDays(day).withHour(hour), 0, 0));
            }
        }
        String expected = """
                \uD83C\uDF21️ Прогноз погоды на неделю вперед:

                12.11.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                
                13.11.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                
                14.11.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                
                15.11.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                
                16.11.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                
                17.11.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)
                
                18.11.2023:
                00-00: 0.0°C (по ощущению 0.0°C)
                04-00: 0.0°C (по ощущению 0.0°C)
                08-00: 0.0°C (по ощущению 0.0°C)
                12-00: 0.0°C (по ощущению 0.0°C)
                16-00: 0.0°C (по ощущению 0.0°C)
                20-00: 0.0°C (по ощущению 0.0°C)""";

        String actual = formatter.formatWeekForecast(weekForecast);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("При пустом прогнозе погоды на неделю вперед должен возвращать только заголовок прогноза погоды")
    void givenWeekEmptyForecast_whenFormatWeekForecast_thenReturnOnlyHeader() {
        List<WeatherForecast> weekForecast = List.of();
        String expected = "\uD83C\uDF21️ Прогноз погоды на неделю вперед:";

        String actual = formatter.formatWeekForecast(weekForecast);

        assertEquals(expected, actual);
    }
}