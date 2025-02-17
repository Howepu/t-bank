package backend.academy.scrapper.services;

import backend.academy.scrapper.stackoverflow.StackOverflowClient;
import backend.academy.scrapper.stackoverflow.StackOverflowQuestion;
import backend.academy.scrapper.repositories.InMemoryLinkRepository;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.*;

class StackOverflowCheckServiceTest {

    @Mock
    private StackOverflowClient stackOverflowClient;

    @Mock
    private InMemoryLinkRepository linkRepository;

    @Mock
    private BotHttpClient botHttpClient;

    private StackOverflowCheckService stackOverflowCheckService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(botHttpClient.sendUpdate(any(LinkUpdate.class))).thenReturn(Mono.empty());
        stackOverflowCheckService = new StackOverflowCheckService(stackOverflowClient, linkRepository, botHttpClient);
    }

    @Test
    void checkLinks_shouldSendUpdateWhenQuestionIsUpdated() {
        // Given
        String url = "https://stackoverflow.com/questions/12345";
        long chatId = 12345L;
        Link link = new Link(chatId, url, null, null);
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        // Преобразуем в Unix timestamp
        Long timestamp = oneDayAgo.toEpochSecond(ZoneOffset.UTC);
        StackOverflowQuestion stackOverflowQuestion = new StackOverflowQuestion(111L,"Question title", timestamp);

        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkRepository.isInitialized(url)).thenReturn(true);
        when(linkRepository.getLastUpdated(url)).thenReturn(LocalDateTime.now().minusDays(2));  // Simulate update
        when(stackOverflowClient.getQuestion("12345")).thenReturn(Mono.just(stackOverflowQuestion));

        stackOverflowCheckService.checkLinks();

        verify(botHttpClient, times(1)).sendUpdate(any(LinkUpdate.class));
    }

    @Test
    void checkLinks_shouldNotSendUpdateWhenQuestionIsNotUpdated() {

        String url = "https://stackoverflow.com/questions/12345";
        long chatId = 12345L;
        Link link = new Link(chatId, url, null, null);
        LocalDateTime now = LocalDateTime.now();
        Long timestamp = now.toEpochSecond(ZoneOffset.UTC);
        StackOverflowQuestion stackOverflowQuestion = new StackOverflowQuestion(111L,"Question title",timestamp);

        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkRepository.isInitialized(url)).thenReturn(true);
        when(linkRepository.getLastUpdated(url)).thenReturn(stackOverflowQuestion.getLastActivityDateAsDateTime());  // Same timestamp
        when(stackOverflowClient.getQuestion("12345")).thenReturn(Mono.just(stackOverflowQuestion));

        stackOverflowCheckService.checkLinks();

        verify(botHttpClient, never()).sendUpdate(any(LinkUpdate.class));
    }

    @Test
    void checkLinks_shouldInitializeLinkWhenFirstTime() {
        // Given
        String url = "https://stackoverflow.com/questions/12345";
        long chatId = 12345L;
        Link link = new Link(chatId, url, null, null);
        LocalDateTime now = LocalDateTime.now();
        Long timestamp = now.toEpochSecond(ZoneOffset.UTC);
        StackOverflowQuestion stackOverflowQuestion = new StackOverflowQuestion(111L,"Question title", timestamp);

        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkRepository.isInitialized(url)).thenReturn(false);  // Link not initialized
        when(stackOverflowClient.getQuestion("12345")).thenReturn(Mono.just(stackOverflowQuestion));

        stackOverflowCheckService.checkLinks();

        verify(linkRepository, times(1)).markInitialized(url);
        verify(linkRepository, times(1)).updateLastUpdated(url, stackOverflowQuestion.getLastActivityDateAsDateTime());
    }
}
