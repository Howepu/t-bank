// package backend.academy.bot.commands;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.mockito.Mockito.*;
//
// import backend.academy.bot.clients.ScrapperHttpClient;
// import backend.academy.bot.dto.LinkResponse;
// import backend.academy.bot.dto.ListLinksResponse;
// import com.pengrad.telegrambot.model.Chat;
// import com.pengrad.telegrambot.model.Message;
// import com.pengrad.telegrambot.model.Update;
// import com.pengrad.telegrambot.request.SendMessage;
// import java.util.List;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import reactor.core.publisher.Mono;
//
// @ExtendWith(MockitoExtension.class)
// class ListCommandTest {
//
//    @Mock
//    private ScrapperHttpClient scrapperHttpClient;
//
//    @InjectMocks
//    private ListCommand listCommand;
//
//    @Test
//    void testHandle_WithLinks() {
//        long chatId = 12345L;
//        Update update = mockUpdate(chatId, "/list");
//
//        ListLinksResponse response = new ListLinksResponse(
//                List.of(
//                        new LinkResponse(chatId, "https://example.com", List.of("tag1"), List.of("filter1")),
//                        new LinkResponse(chatId, "https://github.com", List.of(), List.of())),
//                2);
//
//        when(scrapperHttpClient.getLinks(chatId)).thenReturn(Mono.just(response));
//
//        SendMessage message = listCommand.handle(update);
//
//        String expectedText = "Ваши отслеживаемые ссылки:\n\n- https://example.com\n- https://github.com";
//        assertEquals(chatId, message.getParameters().get("chat_id"));
//        assertEquals(expectedText, message.getParameters().get("text"));
//    }
//
//    @Test
//    void testHandle_EmptyList() {
//        long chatId = 12345L;
//        Update update = mockUpdate(chatId, "/list");
//
//        when(scrapperHttpClient.getLinks(chatId)).thenReturn(Mono.just(new ListLinksResponse(List.of(), 0)));
//
//        SendMessage message = listCommand.handle(update);
//
//        assertEquals(chatId, message.getParameters().get("chat_id"));
//        assertEquals(
//                "Вы пока не отслеживаете ни одной ссылки.",
//                message.getParameters().get("text"));
//    }
//
//    @Test
//    void testHandle_ErrorResponse() {
//        long chatId = 12345L;
//        Update update = mockUpdate(chatId, "/list");
//
//        when(scrapperHttpClient.getLinks(chatId)).thenReturn(Mono.empty());
//
//        SendMessage message = listCommand.handle(update);
//
//        assertEquals(chatId, message.getParameters().get("chat_id"));
//        assertEquals("Ошибка получения списка ссылок.", message.getParameters().get("text"));
//    }
//
//    private Update mockUpdate(Long chatId, String text) {
//        Chat chat = mock(Chat.class);
//        lenient().when(chat.id()).thenReturn(chatId);
//
//        Message message = mock(Message.class);
//        lenient().when(message.chat()).thenReturn(chat);
//        lenient().when(message.text()).thenReturn(text);
//
//        Update update = mock(Update.class);
//        lenient().when(update.message()).thenReturn(message);
//        return update;
//    }
// }
