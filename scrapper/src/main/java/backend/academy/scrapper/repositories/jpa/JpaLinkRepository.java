package backend.academy.scrapper.repositories.jpa;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface JpaLinkRepository extends JpaRepository<Link, Long> {

    List<Link> findAllByChatChatId(Long chatId);

    @Modifying
    @Query("UPDATE Link l SET l.createdAt = :createdAt WHERE l.id = :id")
    void updateCreatedAtById(@Param("id") Long id, @Param("createdAt") LocalDateTime createdAt);

    default Link findAndDeleteByUrl(String url) {
        Link link = findByUrl(url);
        if (link != null) {
            delete(link); // Явно вызываем удаление
        }
        return link;
    }

    Link findByUrl(String url);

    default Link findAndDeleteByChat(Chat chat) {
        Link link = findByChat(chat);
        return link;
    }

    Link findByChat(Chat chat);

    // Получение всех ссылок по тегу
    @Query(
            value =
                    """
        SELECT l FROM Link l
        JOIN l.assignedTags t
        WHERE t.name = :tagName AND l.chat.chatId = :chatId
    """)
    List<Link> findLinksByTag(@Param("tagName") String tagName, @Param("chatId") Long chatId);

    // Привязка тега к ссылке
    @Modifying
    @Query(value = "INSERT INTO link_tag (link_id, tag_id) VALUES (:linkId, :tagId)", nativeQuery = true)
    void attachTagToLink(@Param("linkId") Long linkId, @Param("tagId") Long tagId);

    // Удаление тега у ссылки
    @Modifying
    @Query(value = "DELETE FROM link_tag WHERE link_id = :linkId AND tag_id = :tagId", nativeQuery = true)
    void detachTagFromLink(@Param("linkId") Long linkId, @Param("tagId") Long tagId);

    @Modifying
    @Query(value = "UPDATE link SET known_issue_ids = :issueIds WHERE id = :linkId", nativeQuery = true)
    void updateKnownIssueIds(
            @Param("linkId") Long linkId, @Param("issueIds") Long[] issueIds); // Используем массив вместо List

    @Modifying
    @Query(value = "UPDATE link SET known_pr_ids = :prIds WHERE id = :linkId", nativeQuery = true)
    void updateKnownPrIds(@Param("linkId") Long linkId, @Param("prIds") Long[] prIds); // Используем массив вместо List
}
