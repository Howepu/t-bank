package backend.academy.scrapper.services;

import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        // Мокаем, что чат зарегистрирован и в репозитории есть ссылка для удаления
        when(chatService.isChatRegistered(chatId)).thenReturn(true);
        Link existingLink = new Link(chatId, "http://example.com", List.of("tag1", "tag2"), List.of("filter1", "filter2"));
        when(linkRepository.remove(chatId, "http://example.com")).thenReturn(existingLink);

        // Вызываем сервис для удаления ссылки
        Link removedLink = linkService.removeLink(chatId, "http://example.com");

        // Проверяем, что ссылка была удалена и её данные правильные
        assertNotNull(removedLink);
        assertEquals("http://example.com", removedLink.url());
        assertEquals(2, removedLink.tags().size());
        assertEquals(2, removedLink.filters().size());

        // Проверяем, что метод удаления был вызван
        verify(linkRepository, times(1)).remove(chatId, "http://example.com");
    }
}
