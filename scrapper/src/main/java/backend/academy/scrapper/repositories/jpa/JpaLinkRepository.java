package backend.academy.scrapper.repositories.jpa;

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

    List<Link> findAllByAssignedTagsNameAndChatChatId(String tagName, Long chatId);

    @Modifying
    @Query(value = "UPDATE link SET known_issue_ids = :issueIds WHERE id = :linkId", nativeQuery = true)
    void updateKnownIssueIds(
            @Param("linkId") Long linkId, @Param("issueIds") Long[] issueIds); // Используем массив вместо List

    @Modifying
    @Query(value = "UPDATE link SET known_pr_ids = :prIds WHERE id = :linkId", nativeQuery = true)
    void updateKnownPrIds(@Param("linkId") Long linkId, @Param("prIds") Long[] prIds); // Используем массив вместо List
}
