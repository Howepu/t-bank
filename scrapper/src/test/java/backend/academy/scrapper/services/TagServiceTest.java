package backend.academy.scrapper.services;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Tag;
import java.util.List;
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
public class TagServiceTest {

    @Autowired
    private LinkService linkService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TagService tagService;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE chat, link_tag, link, tag RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("INSERT INTO chat(chat_id) VALUES (123)");
    }

    @Test
    void saveByName_ShouldSaveTag() {
        Chat chat = new Chat(123L);
        tagService.saveByName("tag1", chat.chatId());
        List<Tag> tags = tagService.findAll(chat.chatId());
        assertThat(tags).hasSize(1).extracting(Tag::name).contains("tag1");
    }
}
