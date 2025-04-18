// package backend.academy.bot.commands;
//
// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyLong;
// import static org.mockito.ArgumentMatchers.contains;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
//
// import backend.academy.bot.clients.ScrapperHttpClient;
// import backend.academy.bot.commands.track.TrackCommand;
// import backend.academy.bot.commands.track.TrackDialog;
// import backend.academy.bot.dto.FilterDto;
// import backend.academy.bot.dto.LinkResponse;
// import backend.academy.bot.dto.ListLinksResponse;
// import backend.academy.bot.dto.TagDto;
// import com.pengrad.telegrambot.model.Chat;
// import com.pengrad.telegrambot.model.Update;
// import com.pengrad.telegrambot.request.SendMessage;
// import java.util.List;
// import java.util.Set;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.core.ValueOperations;
// import org.springframework.test.context.TestPropertySource;
// import org.testcontainers.containers.GenericContainer;
// import org.testcontainers.containers.wait.strategy.Wait;
// import org.testcontainers.junit.jupiter.Testcontainers;
// import reactor.core.publisher.Mono;
//
// @SpringBootTest
// @TestPropertySource(
//        properties = {
//            "spring.data.redis.host=${redis.host}",
//            "spring.data.redis.port=${redis.port}",
//            "spring.data.kafka.bootstrap-servers=${kafka.bootstrap-servers}"
//        })
// @Testcontainers
// class ListCommandCacheTest {
//
//    private static GenericContainer<?> redisContainer;
//
//    @BeforeAll
//    static void setup() {
//        redisContainer =
//                new GenericContainer<>("redis:alpine").withExposedPorts(6379).waitingFor(Wait.forListeningPort());
//        redisContainer.start();
//
//        System.setProperty("redis.host", redisContainer.getHost());
//        System.setProperty("redis.port", redisContainer.getMappedPort(6379).toString());
//    }
//
//    @Mock
//    private ScrapperHttpClient scrapperHttpClient;
//
//    @Mock
//    private RedisTemplate<String, String> redisTemplate;
//
//    @Mock
//    private ValueOperations<String, String> valueOperations;
//
//    @InjectMocks
//    private ListCommand listCommand;
//
//    private static final long CHAT_ID = 12345L;
//    private static final String CACHE_KEY = "bot:list:" + CHAT_ID;
//
//    @BeforeEach
//    void setUp() {
//        Update update = mock(Update.class);
//        com.pengrad.telegrambot.model.Message message = mock(com.pengrad.telegrambot.model.Message.class);
//        Chat chat = mock(Chat.class); // создаем мок для Chat
//        when(update.message()).thenReturn(message);
//        when(message.chat()).thenReturn(chat); // chat() возвращает Chat, а не Long
//        when(chat.id()).thenReturn(CHAT_ID); // возвращаем правильный chatId
//    }
//
//    @Test
//    void shouldUseCacheIfPresent() {
//        Update update = mockUpdateWithChatId(CHAT_ID);
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//
//        when(valueOperations.get(CACHE_KEY)).thenReturn("Кэшированный список");
//
//        SendMessage result = listCommand.handle(update);
//
//        assertEquals("Кэшированный список", result.getParameters().get("text"));
//        verify(scrapperHttpClient, never()).getLinks(anyLong());
//    }
//
//    @Test
//    void shouldCallApiAndStoreInCacheIfNotCached() {
//        Update update = mockUpdateWithChatId(CHAT_ID);
//
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//
//        when(valueOperations.get(CACHE_KEY)).thenReturn(null);
//
//        List<LinkResponse> links =
//                List.of(new LinkResponse(1L, "https://example.com", Set.of(new TagDto()), Set.of(new FilterDto())));
//        when(scrapperHttpClient.getLinks(CHAT_ID)).thenReturn(Mono.just(new ListLinksResponse(links, links.size())));
//
//        SendMessage result = listCommand.handle(update);
//
//        assertTrue(result.getParameters().get("text").toString().contains("https://example.com"));
//        verify(scrapperHttpClient).getLinks(CHAT_ID);
//        verify(valueOperations).set(eq(CACHE_KEY), contains("https://example.com"), any());
//    }
//
//    @Test
//    void shouldInvalidateCacheOnUntrack() {
//        // given
//        ScrapperHttpClient client = mock(ScrapperHttpClient.class);
//        RedisTemplate<String, String> redis = mock(RedisTemplate.class);
//        UntrackCommand untrackCommand = new UntrackCommand(client, redis);
//
//        Update update = mockUpdateWithChatIdAndText(CHAT_ID, "/untrack https://example.com");
//        when(client.removeLink(eq(CHAT_ID), any())).thenReturn(Mono.empty());
//
//        SendMessage result = untrackCommand.handle(update);
//
//        verify(redis).delete(CACHE_KEY);
//        assertTrue(result.getParameters().get("text").toString().contains("Ссылка удалена"));
//    }
//
//    @Test
//    void shouldInvalidateCacheOnTrack() {
//        // given
//        TrackDialog dialog = mock(TrackDialog.class);
//        when(dialog.handle(any())).thenReturn(new SendMessage(CHAT_ID, "Добавлено"));
//
//        TrackCommand trackCommand = new TrackCommand(dialog, redisTemplate);
//
//        Update update = mockUpdateWithChatIdAndText(CHAT_ID, "/track https://example.com");
//
//        SendMessage result = trackCommand.handle(update);
//
//        verify(redisTemplate).delete(CACHE_KEY);
//        assertEquals("Добавлено", result.getParameters().get("text"));
//    }
//
//    private Update mockUpdateWithChatId(long chatId) {
//        Update update = mock(Update.class);
//        com.pengrad.telegrambot.model.Message message = mock(com.pengrad.telegrambot.model.Message.class);
//        Chat chat = mock(Chat.class);
//        when(update.message()).thenReturn(message);
//        when(message.chat()).thenReturn(chat);
//        when(chat.id()).thenReturn(chatId);
//        return update;
//    }
//
//    private Update mockUpdateWithChatIdAndText(long chatId, String text) {
//        Update update = mock(Update.class);
//        com.pengrad.telegrambot.model.Message message = mock(com.pengrad.telegrambot.model.Message.class);
//        Chat chat = mock(Chat.class);
//        when(update.message()).thenReturn(message);
//        when(message.chat()).thenReturn(chat);
//        when(chat.id()).thenReturn(chatId);
//        when(message.text()).thenReturn(text);
//        when(message.messageId()).thenReturn(1);
//        return update;
//    }
// }
