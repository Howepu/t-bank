package backend.academy.scrapper.repositories.jdbc;

import backend.academy.scrapper.models.Tag;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@AllArgsConstructor
@Transactional
@Slf4j
public class JdbcTagRepository {

    private final JdbcClient jdbcClient;

    public void add(String name, Long chatId) {
        if (name == null || chatId == null) {
            throw new IllegalArgumentException("Параметры name и chatId не могут быть null");
        }
        String sql = "INSERT INTO tag(name, chat_id) values (?, ?)";
        jdbcClient.sql(sql).param(1, name).param(2, chatId).update();
    }

    public Optional<Tag> getByNameAndChatId(String name, Long chatId) {
        String sql = "SELECT * FROM tag WHERE name = ? AND chat_id = ?";
        log.debug("Поиск тега: name={}, chatId={}", name, chatId);
        try {
            Tag tag = jdbcClient
                    .sql(sql)
                    .param(1, name)
                    .param(2, chatId)
                    .query(Tag.class)
                    .single();
            log.debug("Тег найден: {}", tag);
            return Optional.of(tag);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Тег не найден: name={}, chatId={}", name, chatId);
            return Optional.empty();
        }
    }

    public Tag getByName(String name) {
        String sql = "SELECT * FROM tag WHERE name = ?";
        return jdbcClient.sql(sql).param(1, name).query(Tag.class).single();
    }

    public List<Tag> getAllByChatId(Long chatId) {
        String sql = "SELECT * FROM tag WHERE chat_id = ?";
        return jdbcClient.sql(sql).param(1, chatId).query(Tag.class).list();
    }
}
