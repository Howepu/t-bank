package backend.academy.scrapper.services;

import backend.academy.scrapper.repositories.InMemoryChatRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    @Mock
    private InMemoryChatRepository chatRepository;

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerChat_shouldThrowException_whenChatIdIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            chatService.registerChat(null);
        });
        assertEquals("Chat ID должен быть предоставлен и не может быть пустым", exception.getMessage());
    }

    @Test
    void removeChat_shouldThrowException_whenChatIdNotFound() {
        Long chatId = 123L;
        when(chatRepository.isChatRegistered(chatId)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            chatService.removeChat(chatId);
        });
        assertEquals("Chat ID не найден", exception.getMessage());
    }
}
