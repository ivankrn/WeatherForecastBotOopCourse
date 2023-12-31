package ru.urfu.weatherforecastbot.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.urfu.weatherforecastbot.model.Place;
import ru.urfu.weatherforecastbot.model.WeatherForecast;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты форматировщика прогнозов погоды
 */
class WeatherForecastFormatterImplTest {

    /**
     * Форматировщик прогноза погоды в удобочитаемый вид
     */
    private final WeatherForecastFormatterImpl formatter = new WeatherForecastFormatterImpl();

    @Test
    @DisplayName("При непустом прогнозе погоды на сегодня должен возвращать отформатированный прогноз погоды")
    void givenTodayNotEmptyForecast_whenFormatForecast_thenReturnFormattedTodayForecast() {
        LocalDateTime today = LocalDateTime.of(2023, 11, 5, 0, 0);
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(place, today.withHour(hour), 0, 0));
        }
        String expected = """
                \uD83C\uDF21️ Прогноз погоды на сегодня (Екатеринбург):

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

        String actual = formatter.formatForecasts(ForecastTimePeriod.TODAY, todayForecast);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("При пустом прогнозе погоды должен выбрасывать исключение с сообщением о том, что список " +
            "прогнозов пуст")
    void givenEmptyForecast_whenFormatForecast_thenThrowException() {
        List<WeatherForecast> todayForecast = List.of();

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> formatter.formatForecasts(ForecastTimePeriod.TODAY, todayForecast));

        assertEquals("Forecasts are empty!", exception.getMessage());
    }

    @Test
    @DisplayName("При прогнозе погоды с различными местами должен выбрасывать исключение с сообщением о том, " +
            "что прогнозы относятся не к одному месту")
    void givenForecastWithDifferentPlaces_whenFormatForecast_thenThrowException() {
        LocalDateTime today = LocalDateTime.of(2023, 11, 5, 0, 0);
        int hours = 24;
        List<WeatherForecast> todayForecast = new ArrayList<>(hours);
        Place ekaterinburg =
                new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        Place moscow = new Place("Москва", 55.752, 37.615, "Europe/Moscow");
        for (int hour = 0; hour < hours; hour++) {
            todayForecast.add(
                    new WeatherForecast(
                            hour % 2 == 0 ? ekaterinburg : moscow,
                            today.withHour(hour),
                            0,
                            0)
            );
        }
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> formatter.formatForecasts(ForecastTimePeriod.TODAY, todayForecast));

        assertEquals("Forecasts have different places!", exception.getMessage());
    }

    @Test
    @DisplayName("При непустом прогнозе погоды на завтра должен возвращать отформатированный прогноз погоды")
    void givenTomorrowNotEmptyForecast_whenFormatForecast_thenReturnFormattedTomorrowForecast() {
        LocalDateTime tomorrow = LocalDateTime.of(2023, 11, 5, 0, 0);
        int hours = 24;
        List<WeatherForecast> tomorrowForecast = new ArrayList<>(hours);
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int hour = 0; hour < hours; hour++) {
            tomorrowForecast.add(
                    new WeatherForecast(place, tomorrow.withHour(hour), 0, 0));
        }
        String expected = """
                \uD83C\uDF21️ Прогноз погоды на завтра (Екатеринбург):

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

        String actual = formatter.formatForecasts(ForecastTimePeriod.TOMORROW, tomorrowForecast);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("При непустом прогнозе погоды на неделю вперед должен возвращать отформатированный прогноз погоды")
    void givenWeekNotEmptyForecast_whenFormatForecast_thenReturnFormattedWeekForecast() {
        LocalDateTime today = LocalDateTime.of(2023, 11, 12, 0, 0);
        int days = 7;
        int hourInterval = 4;
        List<WeatherForecast> weekForecast = new ArrayList<>();
        Place place = new Place("Екатеринбург", 56.875, 60.625, "Asia/Yekaterinburg");
        for (int day = 0; day < days; day++) {
            for (int hour = 0; hour < 24; hour += hourInterval) {
                weekForecast.add(
                        new WeatherForecast(place, today.plusDays(day).withHour(hour), 0, 0));
            }
        }
        String expected = """
                \uD83C\uDF21️ Прогноз погоды на неделю (Екатеринбург):

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

        String actual = formatter.formatForecasts(ForecastTimePeriod.WEEK, weekForecast);

        assertEquals(expected, actual);
    }

}