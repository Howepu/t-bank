package backend.academy.scrapper.repositories.jpa;

import backend.academy.scrapper.models.Tag;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface JpaTagRepository extends JpaRepository<Tag, Long> {
    Tag findByName(String name);

    @Modifying
    @Query(value = "INSERT INTO Tag(name, chat_id) VALUES (:name, :chatId)", nativeQuery = true)
    void saveTag(@Param("name") String name, @Param("chatId") Long chatId);

    List<Tag> findAllByChatId(Long chatId);

    Tag findTagByChatId(Long chatId);

    Tag findTagByChatIdAndName(Long chatId, String name);
}
