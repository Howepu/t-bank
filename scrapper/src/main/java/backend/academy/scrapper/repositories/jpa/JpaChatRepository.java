package backend.academy.scrapper.repositories.jpa;

import backend.academy.scrapper.models.Chat;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface JpaChatRepository extends JpaRepository<Chat, Long> {

    @Modifying
    @Query(value = "INSERT INTO chat(chat_id) VALUES (:chatId)", nativeQuery = true)
    void saveByChatId(@Param("chatId") Long chatId);

    boolean existsByChatId(Long chatId);

    Chat getChatByChatId(Long chatId);

    @Query("SELECT c FROM Chat c")
    Set<Chat> getAllChats();

    @Modifying
    @Query("DELETE FROM Chat c WHERE c.chatId = :chatId")
    void deleteByChatId(@Param("chatId") Long chatId);

    Set<Chat> getChatsByMode(String mode);
}
