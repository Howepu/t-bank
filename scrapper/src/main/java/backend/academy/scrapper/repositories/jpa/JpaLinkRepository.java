package backend.academy.scrapper.repositories.jpa;

import backend.academy.scrapper.models.Link;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public interface JpaLinkRepository extends JpaRepository<Link, Long> {

    Link deleteByUrl(String url);

    List<Link> findAllByChat_ChatId(Long chatId);

    @Modifying
    @Query("UPDATE Link l SET l.createdAt = :createdAt WHERE l.id = :id")
    void updateCreatedAtById(@Param("id") Long id, @Param("createdAt") LocalDateTime createdAt);
}
