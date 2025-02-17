package backend.academy.scrapper.repositories;

import backend.academy.scrapper.models.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class InMemoryLinkRepositoryTest {

    private InMemoryLinkRepository linkRepository;

    @BeforeEach
    void setUp() {
        linkRepository = new InMemoryLinkRepository();
    }

    @Test
    void save_shouldStoreLinkInRepository() {
        Link link = new Link(1L, "https://github.com/test/repo", List.of("java", "github"), List.of("commits"));

        Link savedLink = linkRepository.save(1L, link);

        assertNotNull(savedLink);
        assertEquals("https://github.com/test/repo", savedLink.url());
        assertEquals(1, linkRepository.findAllByChatId(1L).size());
    }

    @Test
    void remove_shouldDeleteLinkFromRepository() {
        Link link = new Link(1L, "https://github.com/test/repo", List.of("java", "github"), List.of("commits"));
        linkRepository.save(1L, link);

        Link removedLink = linkRepository.remove(1L, "https://github.com/test/repo");

        assertNotNull(removedLink);
        assertEquals("https://github.com/test/repo", removedLink.url());
        assertTrue(linkRepository.findAllByChatId(1L).isEmpty());
    }

    @Test
    void findAllByChatId_shouldReturnLinksForChat() {
        Link link1 = new Link(1L, "https://github.com/test/repo1", List.of("java"), List.of("commits"));
        Link link2 = new Link(1L, "https://github.com/test/repo2", List.of("java"), List.of("issues"));
        linkRepository.save(1L, link1);
        linkRepository.save(1L, link2);

        List<Link> links = linkRepository.findAllByChatId(1L);

        assertEquals(2, links.size());
        assertTrue(links.contains(link1));
        assertTrue(links.contains(link2));
    }
}
