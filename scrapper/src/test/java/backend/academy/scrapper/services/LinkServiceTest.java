package backend.academy.scrapper.services;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
class LinkServiceTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("scrapper")
            .withUsername("postgres")
            .withPassword("postgres");

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
    void addLink_ShouldAddLink() {
        // Создание объекта Chat
        Chat chat = new Chat(123L); // Предположим, что у вас есть соответствующий конструктор в Chat

        // Указание URL, чата и времени создания
        Link link = new Link("https://example.com", chat);

        // Вызов метода добавления ссылки
        Link addedLink = linkService.addLink(chat.chatId(), link);

        // Проверка результатов
        assertThat(addedLink).isNotNull();
        assertThat(addedLink.url()).isEqualTo("https://example.com");
        assertThat(addedLink.chat().chatId()).isEqualTo(123L);
    }

    @Test
    void removeLink_ShouldRemoveLink() {
        Chat chat = new Chat(123L); // Предположим, что у вас есть соответствующий конструктор в Chat

        // Указание URL, чата и времени создания
        Link link = new Link("https://example.com", chat);
        linkService.addLink(chat.chatId(), link);

        Link removedLink = linkService.removeLink(chat.chatId(), "https://example.com");

        assertThat(removedLink).isNotNull();
        assertThat(removedLink.url()).isEqualTo("https://example.com");

        List<Link> allLinks = linkService.getAllLinks(chat.chatId());
        assertThat(allLinks).doesNotContain(link);
    }

    @Test
    void getAllLinks_ShouldReturnLinks() {
        Chat chat = new Chat(123L); // Предположим, что у вас есть соответствующий конструктор в Chat

        // Указание URL, чата и времени создания
        Link link = new Link("https://example.com", chat);
        Link link2 = new Link("https://example2.com", chat);

        linkService.addLink(chat.chatId(), link);
        linkService.addLink(chat.chatId(), link2);

        List<Link> links = linkService.getAllLinks(chat.chatId());

        assertThat(links).hasSize(2);
        assertThat(links).extracting(Link::url).contains("https://example.com", "https://example2.com");
    }

    @Test
    void findLinksByTag_ShouldReturnLinksWithTag() {
        Chat chat = new Chat(123L); // Предположим, что у вас есть соответствующий конструктор в Chat
        // Указание URL, чата и времени создания
        Link link = new Link("https://example.com", chat);
        Link link2 = new Link("https://example2.com", chat);
        linkService.addLink(chat.chatId(), link);
        linkService.addLink(chat.chatId(), link2);

        tagService.saveByName("tag1", chat.chatId());

        linkService.attachTagToLink(link.url(), "tag1");
        linkService.attachTagToLink(link2.url(), "tag1");

        List<Link> links = linkService.findLinksByTag("tag1", chat.chatId());

        assertThat(links).hasSize(2);
        assertThat(links.get(0).url()).isEqualTo("https://example.com");
    }

    @Test
    void attachTagToLink_ShouldAttachTag() {
        Chat chat = new Chat(123L); // Предположим, что у вас есть соответствующий конструктор в Chat

        // Указание URL, чата и времени создания
        Link link = new Link("https://example.com", chat);
        tagService.saveByName("tag1", chat.chatId());
        Link addedLink = linkService.addLink(chat.chatId(), link);

        assertThat(addedLink.url()).isEqualTo("https://example.com");
        linkService.attachTagToLink(link.url(), "tag1");

        // Проверка, что ссылка теперь имеет тег
        List<Link> linksWithTag = linkService.findLinksByTag("tag1", chat.chatId());
        assertThat(linksWithTag).hasSize(1);
        assertThat(linksWithTag.getFirst().url()).isEqualTo("https://example.com");
    }

    @Test
    void detachTagFromLink_ShouldDetachTag() {
        Chat chat = new Chat(123L); // Предположим, что у вас есть соответствующий конструктор в Chat

        // Указание URL, чата и времени создания
        Link link = new Link("https://example.com", chat);
        linkService.addLink(chat.chatId(), link);
        tagService.saveByName("tag1", chat.chatId());
        linkService.attachTagToLink(link.url(), "tag1");
        List<Link> linksWithTag = linkService.findLinksByTag("tag1", chat.chatId());
        assertThat(linksWithTag).hasSize(1);
        assertThat(linksWithTag.getFirst().url()).isEqualTo("https://example.com");

        linkService.detachTagFromLink(link.url(), "tag1");

        // Проверка, что ссылка больше не имеет тег
        List<Link> linksWithTag1 = linkService.findLinksByTag("tag1", chat.chatId());
        assertThat(linksWithTag1).isEmpty();
    }
}
