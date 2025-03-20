package backend.academy.scrapper.repositories.jpa;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface JpaLinkRepository extends JpaRepository<Link, Long> {

    List<Link> findAllByChatChatId(Long chatId);

    @Modifying
    @Query("UPDATE Link l SET l.createdAt = :createdAt WHERE l.id = :id")
    void updateCreatedAtById(@Param("id") Long id, @Param("createdAt") LocalDateTime createdAt);

    default Link findAndDeleteByUrl(String url) {
        Link link = findByUrl(url);
        if (link != null) {
            delete(link);  // Явно вызываем удаление
        }
        return link;
    }
    Link findByUrl(String url);

    default Link findAndDeleteByChat(Chat chat) {
        Link link = findByChat(chat);
        return link;
    }

    Link findByChat(Chat chat);

}
