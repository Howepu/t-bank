package backend.academy.scrapper.repositories.jdbc;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Filter;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.Tag;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@AllArgsConstructor
@Transactional
@Slf4j
public class JdbcLinkRepository {

    private final JdbcClient jdbcClient;

    private Link mapLink(ResultSet rs) throws SQLException {
        Link link = new Link();
        link.id(rs.getLong("id"));
        link.url(rs.getString("url"));
        link.createdAt(null);
        link.chat(new Chat(rs.getLong("chat_id")));
        Array issueIdsArray = rs.getArray("known_issue_ids");
        link.knownIssueIds(
                issueIdsArray != null ? Arrays.asList((Long[]) issueIdsArray.getArray()) : new ArrayList<>());

        Array prIdsArray = rs.getArray("known_pr_ids");
        link.knownPrIds(prIdsArray != null ? Arrays.asList((Long[]) prIdsArray.getArray()) : new ArrayList<>());

        return link;
    }

    public List<Link> findLinksByTag(String tagName, Long chatId) {
        String sql =
                """
    SELECT l.id, l.url, l.created_at, l.known_issue_ids, l.known_pr_ids, l.chat_id
    FROM link l
    JOIN link_tag lt ON l.id = lt.link_id
    JOIN tag t ON lt.tag_id = t.id
    WHERE t.name = ? AND l.chat_id = ?
    """;

        List<Link> links = jdbcClient
                .sql(sql)
                .param(1, tagName)
                .param(2, chatId)
                .query((rs, rowNum) -> mapLink(rs))
                .list();

        for (Link link : links) {
            Set<Tag> tags = findTagsByLinkId(link.id());
            link.assignedTags(tags);
        }

        return links;
    }

    public List<Link> findAllLinksByChatId(Long chatId) {
        String sql =
                """
    SELECT l.id, l.url, l.created_at, l.known_issue_ids, l.known_pr_ids, l.chat_id
    FROM link l
    WHERE l.chat_id = :chatId
    """;

        List<Link> links = jdbcClient
                .sql(sql)
                .param("chatId", chatId)
                .query((rs, rowNum) -> mapLink(rs))
                .list();

        for (Link link : links) {
            Set<Tag> tags = findTagsByLinkId(link.id());
            link.assignedTags(tags);
        }

        return links;
    }

    private Set<Tag> findTagsByLinkId(Long linkId) {
        String sql =
                """
        SELECT t.id, t.name, t.chat_id
        FROM tag t
        JOIN link_tag lt ON t.id = lt.tag_id
        WHERE lt.link_id = :linkId
    """;

        return jdbcClient
                .sql(sql)
                .param("linkId", linkId)
                .query((rs, rowNum) -> {
                    Tag tag = new Tag();
                    tag.id(rs.getLong("id"));
                    tag.name(rs.getString("name"));
                    tag.chatId(rs.getLong("chat_id"));
                    return tag;
                })
                .set();
    }

    public Link addLink(Link link, Long chatId) {

        String sql = "INSERT INTO link (url, chat_id, created_at) VALUES (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcClient
                .sql(sql)
                .param(1, link.url())
                .param(2, chatId)
                .param(3, link.createdAt())
                .update(keyHolder, new String[] {"id"});

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Failed to insert link, no ID obtained.");
        }
        Long linkId = key.longValue();

        if (link.assignedTags() != null && !link.assignedTags().isEmpty()) {
            for (Tag tag : link.assignedTags()) {

                tag.chatId(chatId);

                Long tagId = getTagIdByNameAndChatId(tag.name(), chatId);

                String insertLinkTagSql = "INSERT INTO link_tag (link_id, tag_id) VALUES (?, ?)";
                jdbcClient
                        .sql(insertLinkTagSql)
                        .param(1, linkId) // ID только что добавленной ссылки
                        .param(2, tagId) // ID тега
                        .update();
            }
        }

        if (link.assignedFilters() != null && !link.assignedFilters().isEmpty()) {
            for (Filter filter : link.assignedFilters()) {

                filter.chatId(chatId);

                Long filterId = getFilterIdByNameAndChatId(filter.name(), chatId);

                String insertLinkTagSql = "INSERT INTO link_filter (link_id, filter_id) VALUES (?, ?)";
                jdbcClient
                    .sql(insertLinkTagSql)
                    .param(1, linkId)
                    .param(2, filterId)
                    .update();
            }
        }

        return link;
    }

    private Long getTagIdByNameAndChatId(String tagName, Long chatId) {
        return jdbcClient
                .sql("SELECT id FROM tag WHERE name = :name AND chat_id = :chatId")
                .param("name", tagName)
                .param("chatId", chatId)
                .query(Long.class)
                .optional()
                .orElseGet(() -> {
                    // Если тег не найден, создаем новый
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    jdbcClient
                            .sql("INSERT INTO tag (name, chat_id) VALUES (:name, :chatId)")
                            .param("name", tagName)
                            .param("chatId", chatId)
                            .update(keyHolder, new String[] {"id"});
                    return keyHolder.getKey().longValue();
                });
    }

    private Long getFilterIdByNameAndChatId(String filterName, Long chatId) {
        return jdbcClient
            .sql("SELECT id FROM filter WHERE name = :name AND chat_id = :chatId")
            .param("name", filterName)
            .param("chatId", chatId)
            .query(Long.class)
            .optional()
            .orElseGet(() -> {
                // Если тег не найден, создаем новый
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcClient
                    .sql("INSERT INTO filter (name, chat_id) VALUES (:name, :chatId)")
                    .param("name", filterName)
                    .param("chatId", chatId)
                    .update(keyHolder, new String[] {"id"});
                return keyHolder.getKey().longValue();
            });
    }

    // Удаление ссылки
    public Link deleteByUrl(String url) {
        Link link = findByUrl(url);
        if (link != null) {
            jdbcClient
                    .sql("DELETE FROM link_tag WHERE link_id = :linkId")
                    .param("linkId", link.id())
                    .update();
            jdbcClient
                    .sql("DELETE FROM link WHERE id = :linkId")
                    .param("linkId", link.id())
                    .update();
        }

        return link;
    }

    // Найти ссылку по URL
    public Link findByUrl(String url) {
        return jdbcClient
                .sql("SELECT link.url, link.id FROM link WHERE url = :url")
                .param("url", url)
                .query(Link.class)
                .single();
    }

    // Обновление времени создания ссылки
    public void updateCreatedAt(Long id, LocalDateTime createdAt) {
        jdbcClient
                .sql("UPDATE link SET created_at = :createdAt WHERE id = :id")
                .param("id", id)
                .param("createdAt", Timestamp.valueOf(createdAt))
                .update();
    }

    // Привязка тега к ссылке
    public void attachTagToLink(Long linkId, Long tagId) {
        jdbcClient
                .sql("INSERT INTO link_tag (link_id, tag_id) VALUES (:linkId, :tagId)")
                .param("linkId", linkId)
                .param("tagId", tagId)
                .update();
    }

    // Удаление тега у ссылки
    public void detachTagFromLink(Long linkId, Long tagId) {
        jdbcClient
                .sql("DELETE FROM link_tag WHERE link_id = :linkId AND tag_id = :tagId")
                .param("linkId", linkId)
                .param("tagId", tagId)
                .update();
    }

    public void updateLinkIssues(Long linkId, List<Long> issueIds) {
        String sql = "UPDATE link SET known_issue_ids = ? WHERE id = ?";
        jdbcClient
                .sql(sql)
                .param(1, issueIds.toArray(new Long[0]))
                .param(2, linkId)
                .update();
    }

    public void updateLinkPullRequests(Long linkId, List<Long> prIds) {
        String sql = "UPDATE link SET known_pr_ids = ? WHERE id = ?";
        jdbcClient
                .sql(sql)
                .param(1, prIds.toArray(new Long[0]))
                .param(2, linkId)
                .update();
    }

    public List<String> getFilterNameByLink(Long linkId) {
        String sql = "SELECT name FROM filter JOIN link_filter ON filter.id = link_filter.filter_id WHERE link_filter.link_id = ?";
        return jdbcClient.sql(sql)
            .param(1, linkId)
            .query(String.class)
            .list();
    }
}
