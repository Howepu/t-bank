package backend.academy.scrapper.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.repositories.LinkRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private LinkService linkService;

    private Long chatId;
    private Link link;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatId = 123L;
        link = new Link(chatId, "http://example.com", List.of("tag1", "tag2"), List.of("filter1", "filter2"));
    }

    @Test
    void testAddLink() {
        when(chatService.isChatRegistered(chatId)).thenReturn(true);
        when(linkRepository.save(chatId, link)).thenReturn(link);

        Link savedLink = linkService.addLink(chatId, link);

        assertNotNull(savedLink);
        assertEquals("http://example.com", savedLink.url());
        assertEquals(2, savedLink.tags().size());
        assertEquals(2, savedLink.filters().size());

        verify(linkRepository, times(1)).save(chatId, link);
    }

    @Test
    void testAddLinkChatNotRegistered() {
        when(chatService.isChatRegistered(chatId)).thenReturn(false);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            linkService.addLink(chatId, link);
        });

        assertEquals("Чат не зарегистрирован", thrown.getMessage());
        verify(linkRepository, never()).save(anyLong(), any());
    }

    @Test
    void testRemoveLinkHappyPath() {
        when(chatService.isChatRegistered(chatId)).thenReturn(true);
        Link existingLink =
                new Link(chatId, "http://example.com", List.of("tag1", "tag2"), List.of("filter1", "filter2"));
        when(linkRepository.remove(chatId, "http://example.com")).thenReturn(existingLink);

        Link removedLink = linkService.removeLink(chatId, "http://example.com");

        assertNotNull(removedLink);
        assertEquals("http://example.com", removedLink.url());
        assertEquals(2, removedLink.tags().size());
        assertEquals(2, removedLink.filters().size());

        // Проверяем, что метод удаления был вызван
        verify(linkRepository, times(1)).remove(chatId, "http://example.com");
    }

    @Test
    void addLink_shouldThrowException_whenChatNotRegistered() {
        Long chatId = 123L;
        Link link = new Link(chatId, "https://github.com/test/repo", null, null);

        when(chatService.isChatRegistered(chatId)).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            linkService.addLink(chatId, link);
        });
        assertEquals("Чат не зарегистрирован", exception.getMessage());
    }

    @Test
    void addLink_shouldThrowException_whenChatIdIsNull() {
        Link link = new Link(null, "https://github.com/test/repo", null, null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            linkService.addLink(null, link);
        });
        assertEquals("Чат не зарегистрирован", exception.getMessage());
    }

    @Test
    void removeLink_shouldThrowException_whenChatNotRegistered() {
        Long chatId = 123L;
        String url = "https://github.com/test/repo";

        when(chatService.isChatRegistered(chatId)).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            linkService.removeLink(chatId, url);
        });
        assertEquals("Чат не зарегистрирован", exception.getMessage());
    }

    @Test
    void removeLink_shouldThrowException_whenLinkNotFound() {
        Long chatId = 123L;
        String url = "https://github.com/test/repo";

        when(chatService.isChatRegistered(chatId)).thenReturn(true);
        when(linkRepository.remove(chatId, url)).thenReturn(null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            linkService.removeLink(chatId, url);
        });
        assertEquals("Ссылка не найдена", exception.getMessage());
    }

    @Test
    void getAllLinks_shouldThrowException_whenChatIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            linkService.getAllLinks(null);
        });
        assertEquals("Chat ID должен быть предоставлен и не может быть пустым", exception.getMessage());
    }
}
