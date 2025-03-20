package backend.academy.scrapper.repositories.jdbc;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
@AllArgsConstructor
public class JdbcLinkRepository {

    private final JdbcClient jdbcClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<Link> findAllByChatChatId(Long chatId) {
        String sql = "SELECT id, url, created_at, chat_id, array_to_json(filters) AS filters FROM link WHERE chat_id = ?";
        List<Link> links = jdbcClient.sql(sql)
            .param(1, chatId)
            .query((rs, rowNum) -> {
                Long retrievedChatId = rs.getLong("chat_id");
                System.out.println("Retrieved chat_id: " + retrievedChatId); // Логирование
                return new Link(
                    rs.getLong("id"),
                    rs.getString("url"),
                    parseJsonArray(rs.getString("filters")),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                    new Chat(retrievedChatId)
                );
            })
            .list();
        return links;
    }


    public void updateCreatedAtById(Long id, LocalDateTime createdAt) {
        String sql = "UPDATE link SET created_at = ? WHERE id = ?";
        jdbcClient.sql(sql)
            .param(1, createdAt)
            .param(2, id)
            .update();
    }

    public Link findByUrl(String url) {
        String sql = "SELECT * FROM link WHERE url = ?";
        return jdbcClient.sql(sql)
            .param(1, url)
            .query(new LinkRowMapper())
            .optional()
            .orElse(null);
    }

    public Link findAndDeleteByChat(Chat chat) {
        return findByChat(chat);
    }

    public Link findByChat(Chat chat) {
        String sql = "SELECT * FROM link WHERE chat_id = ?";
        return jdbcClient.sql(sql)
            .param(1, chat.chatId())
            .query(new LinkRowMapper())
            .optional()
            .orElse(null);
    }

    public int delete(String url) {
        String sql = "DELETE FROM link WHERE url = ?";
        return jdbcClient.sql(sql)
            .param(1, url)
            .update();
    }

    public Link add(Link link) {
        String sql = "INSERT INTO link (url, filters, created_at, chat_id) VALUES (?, ?, ?, ?)";

        jdbcClient.sql(sql)
            .param(1, link.url())
            .param(2, link.filters().toArray(new String[0]))
            .param(3, link.createdAt())
            .param(4, link.chat().chatId())
            .update();

        return link;
    }

    private List<String> parseJsonArray(String json) {
        try {
            return json == null ? List.of() : objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при парсинге JSON", e);
        }
    }
}
