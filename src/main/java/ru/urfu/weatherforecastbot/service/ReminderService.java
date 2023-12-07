package ru.urfu.weatherforecastbot.service;

import ru.urfu.weatherforecastbot.model.Reminder;

import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Сервис для управления напоминаниями
 */
public interface ReminderService {

    /**
     * Возврат всех установленных напоминаний для определенного чата
     *
     * @param chatId ID чата
     * @return все установленные напоминания
     */
    List<Reminder> findAllForChatId(long chatId);

    /**
     * Добавление напоминания для определенного чата, места и времени (UTC - стандарт)
     *
     * @param chatId    ID чата
     * @param placeName название места
     * @param time      время в строковом виде (UTC - стандарт)
     * @throws DateTimeParseException если произошла ошибка паркинга
     */
    void addReminder(long chatId, String placeName, String time) throws DateTimeParseException;

    /**
     * Редактирование напоминаний под указанным номером для определенного чата, места и времени (UTC - стандарт)
     *
     * @param chatId       ID чата
     * @param position     позиция напоминания в списке
     * @param newPlaceName новое место
     * @param newTime      новое время в строковом виде (UTC - стандарт)
     * @throws DateTimeParseException если произошла ошибка паркинга
     * @throws IllegalArgumentException если позиция неправильная
     */
    void editReminderByRelativePosition(long chatId, int position, String newPlaceName, String newTime)
            throws DateTimeParseException, IllegalArgumentException;

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
