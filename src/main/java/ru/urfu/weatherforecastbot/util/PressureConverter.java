package ru.urfu.weatherforecastbot.util;

/**
 * Конвертер давления
 */
// TODO: 02.11.2023 Спросить, нужно ли делать интерфейс в таком случае, и должно ли быть статиком и нужны ли тесты
public class PressureConverter {

    /**
     * Перевести давление из гектопаскалей (hPa) в мм рт. ст. (mm Hg)
     *
     * @param pressureInHpa давление в гектопаскалях
     * @return давление в мм рт. ст.
     */
    public static double convertHpaToMmhg(double pressureInHpa) {
        return pressureInHpa * 0.75;
    }

}
