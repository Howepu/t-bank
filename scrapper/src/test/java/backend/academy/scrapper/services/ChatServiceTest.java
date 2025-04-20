package backend.academy.scrapper.services;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.models.Chat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
class ChatServiceTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres");

    @Autowired
    private ChatService chatService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM link_tag");
        jdbcTemplate.execute("DELETE FROM link");
        jdbcTemplate.execute("DELETE FROM chat");
        jdbcTemplate.execute("DELETE FROM tag");
    }

    @Test
    void registerChat_ShouldSaveChat() {
        Long chatId = 123L;

        chatService.registerChat(chatId);
        boolean exists = chatService.isChatRegistered(chatId);

        assertThat(exists).isTrue();
    }

    @Test
    void getChatById_ShouldReturnChat_WhenExists() {
        Chat chat = new Chat(123L, null);
        chatService.registerChat(chat.chatId());

        Chat result = chatService.getChatById(123L);

        assertThat(result).isNotNull();
        assertThat(result.chatId()).isEqualTo(123L);
    }
}
