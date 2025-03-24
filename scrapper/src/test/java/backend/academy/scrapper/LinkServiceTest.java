//package backend.academy.scrapper;
//
//import backend.academy.scrapper.models.Link;
//import backend.academy.scrapper.services.ChatService;
//import backend.academy.scrapper.services.LinkService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import java.time.LocalDateTime;
//import java.util.List;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//@SpringBootTest
//@Testcontainers
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//public class LinkServiceTest {
//
//    @Autowired
//    private LinkService linkService;
//
//    @Autowired
//    private ChatService chatService;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate; // Add this
//
//    @BeforeEach
//    void setUp() {
//        // Clean the database before each test
//        jdbcTemplate.execute("DELETE FROM chat");
//        jdbcTemplate.execute("DELETE FROM link");
//    }
//
//    @Test
//    void testAddLink() {
//        Long chatId = System.currentTimeMillis(); // Use a unique ID
//        chatService.registerChat(chatId);
//
//        Link link = new Link("https://example.com");
//        Link savedLink = linkService.addLink(chatId, link);
//
//        assertNotNull(savedLink);
//        assertEquals(link, savedLink.url());
//    }
//
//    @Test
//    void testRemoveLink() {
//        Long chatId = 1L;
//        chatService.registerChat(chatId);
//
//        Link link = new Link("https://example.com");
//        linkService.addLink(chatId, link);
//
//        Link removedLink = linkService.removeLink(chatId, "https://example.com");
//        assertNotNull(removedLink);
//
//        List<Link> links = linkService.getAllLinks(chatId);
//        assertTrue(links.isEmpty());
//    }
//
//    @Test
//    void testUpdateLastUpdated() {
//        Long chatId = 1L;
//        chatService.registerChat(chatId);
//
//        Link link = new Link("https://example.com");
//        Link savedLink = linkService.addLink(chatId, link);
//
//        LocalDateTime newTime = LocalDateTime.now();
//        linkService.updateLastUpdated(savedLink.id(), newTime);
//
//        List<Link> links = linkService.getAllLinks(chatId);
//        assertEquals(newTime, links.get(0).createdAt());
//    }
//
//    @Test
//    void testAttachAndDetachTag() {
//        Long chatId = 1L;
//        chatService.registerChat(chatId);
//
//        Link link = new Link("https://example.com");
//        linkService.addLink(chatId, link);
//
//        linkService.attachTagToLink("https://example.com", "important");
//        List<Link> linksWithTag = linkService.findLinksByTag("important", chatId);
//        assertEquals(1, linksWithTag.size());
//
//        linkService.detachTagFromLink("https://example.com", "important");
//        linksWithTag = linkService.findLinksByTag("important", chatId);
//        assertTrue(linksWithTag.isEmpty());
//    }
//}
//
