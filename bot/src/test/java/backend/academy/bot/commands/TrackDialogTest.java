package backend.academy.bot.commands;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.commands.track.TrackDialog;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Chat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrackDialogTest {

    private ScrapperHttpClient scrapperHttpClient;
    private TrackDialog trackDialog;

    @BeforeEach
    void setUp() {
        scrapperHttpClient = Mockito.mock(ScrapperHttpClient.class);
        trackDialog = new TrackDialog(scrapperHttpClient);
    }

    private Update mockUpdate(Long chatId, String messageText) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn(messageText);
        when(message.messageId()).thenReturn(123);

        return update;
    }

    @Test
    void testTrackCommand_ValidUrl() {
        Long chatId = 12345L;
        Update update = mockUpdate(chatId, "/track https://github.com/example/repo");

        when(scrapperHttpClient.addLink(anyLong(), any(AddLinkRequest.class)))
            .thenReturn(Mono.empty());

        var response = trackDialog.handle(update);
        assertNotNull(response);
        assertEquals("Введите тэги через запятую (опционально). Введите - для пропуска",
            response.getParameters().get("text"));

        update = mockUpdate(chatId, "java, backend");
        response = trackDialog.handle(update);
        assertEquals("Настройте фильтры через запятую (опционально). Введите - для пропуска",
            response.getParameters().get("text"));

        update = mockUpdate(chatId, "-");
        response = trackDialog.handle(update);

        assertTrue(response.getParameters().get("text").toString().contains("Ссылка добавлена"));
        verify(scrapperHttpClient, times(1)).addLink(eq(chatId), any(AddLinkRequest.class));
    }

    @Test
    void testTrackCommand_InvalidUrl() {
        Long chatId = 12345L;
        Update update = mockUpdate(chatId, "/track https://example.com/not-supported");

        var response = trackDialog.handle(update);
        assertNotNull(response);
        assertEquals("Неверный формат ссылки. Поддерживаются только GitHub и StackOverflow.", response.getParameters().get("text"));
    }

    @Test
    void testTrackCommand_DuplicateUrl() {
        Long chatId = 12345L;
        String duplicateUrl = "https://github.com/example/repo";

        // Моделируем ответ с уже существующей ссылкой
        ListLinksResponse linksResponse = new ListLinksResponse(List.of(
            new LinkResponse(1L, duplicateUrl, null, null)
        ), 1);
        when(scrapperHttpClient.getLinks(chatId)).thenReturn(Mono.just(linksResponse));

        // Пользователь пытается добавить уже существующую ссылку
        Update update = mockUpdate(chatId, "/track " + duplicateUrl);
        var response = trackDialog.handle(update);

        // Проверяем, что пользователь получил сообщение о дублировании
        assertNotNull(response);
        assertEquals("Эта ссылка уже отслеживается.", response.getParameters().get("text"));

        // Убеждаемся, что метод addLink не вызывался
        verify(scrapperHttpClient, never()).addLink(eq(chatId), any(AddLinkRequest.class));
    }

}

