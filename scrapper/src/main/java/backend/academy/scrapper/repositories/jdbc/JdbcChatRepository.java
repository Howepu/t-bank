package backend.academy.scrapper.repositories.jdbc;

import backend.academy.scrapper.models.Chat;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Transactional
@Repository
public class JdbcChatRepository {

    private final JdbcClient jdbcClient;

    public void add(Long chatId) {
        jdbcClient.sql("INSERT INTO chat(chat_id) values (?)").param(1, chatId).update();
    }

    public void remove(Long chatId) {
        String sql = "DELETE FROM chat WHERE chat_id = ?";
        jdbcClient.sql(sql).param(1, chatId).update();
    }

    public Chat getByChatId(Long chatId) {
        String sql = "SELECT * FROM chat WHERE chat_id = ?";
        return jdbcClient.sql(sql).param(1, chatId).query(Chat.class).stream()
                .findFirst()
                .orElse(null);
    }

    public Set<Chat> getAll() {
        String sql = "SELECT * FROM chat";
        return jdbcClient.sql(sql).query(Chat.class).set();
    }

    public void updateMode(Long chatId, String mode) {
        String sql = "UPDATE chat SET mode = ? WHERE chat_id = ?";
        jdbcClient.sql(sql).param(1, mode).param(2, chatId).update();
    }

    public Set<Chat> getChatsByMode(String mode) {
        String sql = "SELECT * FROM chat WHERE mode = ?";
        return jdbcClient
                .sql(sql)
                .param(1, mode.toLowerCase())
                .query(Chat.class)
                .set();
    }
}
