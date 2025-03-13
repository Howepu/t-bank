//package backend.academy.scrapper.repositories;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//public class InMemoryChatRepositoryTest {
//
//    private InMemoryChatRepository chatRepository;
//
//    @BeforeEach
//    void setUp() {
//        chatRepository = new InMemoryChatRepository();
//    }
//
//    @Test
//    void save_shouldAddChat() {
//        Long chatId = 12345L;
//
//        chatRepository.save(chatId);
//
//        assertTrue(chatRepository.isChatRegistered(chatId));
//    }
//
//    @Test
//    void remove_shouldRemoveChat() {
//        Long chatId = 12345678L;
//
//        chatRepository.save(chatId);
//        assertTrue(chatRepository.isChatRegistered(chatId)); // Убеждаемся, что чат добавлен
//
//        chatRepository.remove(chatId);
//
//        assertFalse(chatRepository.isChatRegistered(chatId), "Chat should be removed");
//    }
//
//    @Test
//    void isChatRegistered_shouldReturnFalseWhenChatNotExists() {
//        Long chatId = 12323345L;
//
//        assertFalse(chatRepository.isChatRegistered(chatId));
//    }
//
//    @Test
//    void isChatRegistered_shouldReturnTrueWhenChatExists() {
//        Long chatId = 12345L;
//
//        chatRepository.save(chatId);
//
//        assertTrue(chatRepository.isChatRegistered(chatId));
//    }
//}
