package backend.academy.scrapper.services;

import backend.academy.scrapper.models.Chat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatService chatService;

    @Test
    void registerChat_ShouldCallServiceMethod() {
        // Arrange
        Long chatId = 123L;

        // Act
        chatService.registerChat(chatId);

        // Assert
        verify(chatService, times(1)).registerChat(chatId);
    }

    @Test
    void isChatRegistered_ShouldReturnTrue_WhenChatExists() {
        // Arrange
        Long chatId = 123L;
        when(chatService.isChatRegistered(chatId)).thenReturn(true);

        // Act
        boolean result = chatService.isChatRegistered(chatId);

        // Assert
        assertTrue(result);
        verify(chatService, times(1)).isChatRegistered(chatId);
    }

    @Test
    void getChatById_ShouldReturnChat_WhenExists() {
        // Arrange
        Long chatId = 123L;
        Chat expectedChat = new Chat(chatId);
        when(chatService.getChatById(chatId)).thenReturn(expectedChat);

        // Act
        Chat result = chatService.getChatById(chatId);

        // Assert
        assertEquals(expectedChat, result);
        verify(chatService, times(1)).getChatById(chatId);
    }

    @Test
    void removeChat_ShouldCallServiceMethod() {
        // Arrange
        Long chatId = 123L;

        // Act
        chatService.removeChat(chatId);

        // Assert
        verify(chatService, times(1)).removeChat(chatId);
    }

    @Test
    void getAllRegisteredChatIds_ShouldReturnSetOfChats() {
        // Arrange
        Set<Chat> expectedChats = Set.of(new Chat(123L), new Chat(456L));
        when(chatService.getAllRegisteredChatIds()).thenReturn(expectedChats);

        // Act
        Set<Chat> result = chatService.getAllRegisteredChatIds();

        // Assert
        assertEquals(expectedChats, result);
        verify(chatService, times(1)).getAllRegisteredChatIds();
    }
}
