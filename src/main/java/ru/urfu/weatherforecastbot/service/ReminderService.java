package ru.urfu.weatherforecastbot.service;

import java.time.format.DateTimeParseException;

/**
 * Сервис для управления напоминаниями
 */
public interface ReminderService {

    /**
     * Добавление напоминания для определенного чата, места и времени (UTC - стандарт)
     *
     * @param chatId    ID чата
     * @param placeName название места
     * @param time      время в строковом виде (UTC - стандарт)
     * @throws DateTimeParseException если произошла ошибка парсинга
     */
    void addReminder(long chatId, String placeName, String time) throws DateTimeParseException;

    /**
     * Удаляет напоминание по относительному положению в списке напоминаний пользователя.
     * <p>Пример: если список напоминаний пользователя с ID чата 1 выглядит так:
     * <ol>
     *     <li>Екатеринбург, 06:00</li>
     *     <li>Екатеринбург, 17:00</li>
     * </ol>
     * то вызов {@code deleteReminderByRelativePosition(1, 2)} удалит напоминание под номером 2 (т.е. со временем 17:00)
     * </p>
     *
     * @param chatId   ID чата
     * @param position относительная позиция напоминания в списке
     * @throws IllegalArgumentException если нет напоминания с такой позицией
     */
    void deleteReminderByRelativePosition(long chatId, int position) throws IllegalArgumentException;

}
