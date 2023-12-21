package ru.urfu.weatherforecastbot.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.weatherforecastbot.model.Reminder;

import java.util.List;

/**
 * Репозиторий напоминаний
 */
@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    /**
     * Возвращает все установленные напоминания для указанного чата
     *
     * @param chatId ID чата
     * @return все установленные напоминания
     */
    List<Reminder> findAllByChatId(long chatId);


}
