package ru.urfu.weatherforecastbot.database;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.weatherforecastbot.model.ChatContext;

/**
 * Репозиторий контекстов чатов
 */
@Repository
public interface ChatContextRepository extends CrudRepository<ChatContext, Long> {
}
