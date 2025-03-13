//package backend.academy.scrapper.services;
//
//import static org.mockito.Mockito.*;
//
//import backend.academy.scrapper.bot.BotHttpClient;
//import backend.academy.scrapper.dto.LinkUpdate;
//import backend.academy.scrapper.models.Link;
//import backend.academy.scrapper.repositories.InMemoryLinkRepository;
//import backend.academy.scrapper.stackoverflow.StackOverflowClient;
//import backend.academy.scrapper.stackoverflow.StackOverflowQuestion;
//import java.time.LocalDateTime;
//import java.util.Collections;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import reactor.core.publisher.Mono;
//
//public class StackOverflowCheckServiceTest {
//
//    @Mock
//    private StackOverflowClient stackOverflowClient;
//
//    @Mock
//    private InMemoryLinkRepository linkRepository;
//
//    @Mock
//    private BotHttpClient botHttpClient;
//
//    @InjectMocks
//    private StackOverflowCheckService stackOverflowCheckService;
//
//    @Mock
//    private Link link;
//
//    @Mock
//    private StackOverflowQuestion stackOverflowQuestion;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testCheckLinks_withUpdatedQuestion() {
//        String testUrl = "https://stackoverflow.com/questions/12345";
//        Long chatId = 123L;
//        LocalDateTime lastUpdated = LocalDateTime.now().minusDays(1);
//
//        when(linkRepository.findAll()).thenReturn(Collections.singletonList(link));
//        when(link.url()).thenReturn(testUrl);
//        when(link.chatId()).thenReturn(chatId);
//        when(linkRepository.isInitialized(testUrl)).thenReturn(true);
//        when(linkRepository.getLastUpdated(testUrl)).thenReturn(lastUpdated);
//
//        when(stackOverflowClient.getQuestion("12345"))
//                .thenReturn(Mono.just(new StackOverflowQuestion(12345L, "Test Question", System.currentTimeMillis())));
//
//        when(botHttpClient.sendUpdate(any(LinkUpdate.class))).thenReturn(Mono.empty());
//
//        stackOverflowCheckService.checkLinks();
//
//        verify(linkRepository, times(1)).updateLastUpdated(eq(testUrl), any());
//        verify(botHttpClient, times(1)).sendUpdate(any(LinkUpdate.class));
//    }
//
//    @Test
//    void testCheckLinks_withNewQuestion() {
//        String testUrl = "https://stackoverflow.com/questions/67890";
//        Long chatId = 123L;
//
//        when(linkRepository.findAll()).thenReturn(Collections.singletonList(link));
//        when(link.url()).thenReturn(testUrl);
//        when(link.chatId()).thenReturn(chatId);
//        when(linkRepository.isInitialized(testUrl)).thenReturn(false);
//
//        StackOverflowQuestion stackOverflowQuestion =
//                new StackOverflowQuestion(67890L, "New Test Question", System.currentTimeMillis());
//        when(stackOverflowClient.getQuestion("67890")).thenReturn(Mono.just(stackOverflowQuestion));
//
//        stackOverflowCheckService.checkLinks();
//
//        verify(linkRepository, times(1)).markInitialized(testUrl);
//        verify(linkRepository, times(1)).updateLastUpdated(eq(testUrl), any());
//        verify(botHttpClient, times(0)).sendUpdate(any());
//    }
//}
