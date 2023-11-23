package ru.urfu.weatherforecastbot.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.weatherforecastbot.model.ChatState;

/**
 * Репозиторий состояний чатов
 */
@Repository
public interface ChatStateRepository extends CrudRepository<ChatState, Long> {
}
