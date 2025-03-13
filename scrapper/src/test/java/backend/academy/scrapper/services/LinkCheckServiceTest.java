//package backend.academy.scrapper.services;
//
//import static org.mockito.Mockito.*;
//
//import backend.academy.scrapper.bot.BotHttpClient;
//import backend.academy.scrapper.dto.LinkUpdate;
//import backend.academy.scrapper.github.GitHubClient;
//import backend.academy.scrapper.github.GitHubRepository;
//import backend.academy.scrapper.models.Link;
//import backend.academy.scrapper.repositories.InMemoryLinkRepository;
//import java.time.LocalDateTime;
//import java.util.List;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.*;
//import reactor.core.publisher.Mono;
//
//class LinkCheckServiceTest {
//
//    @Mock
//    private GitHubClient gitHubClient;
//
//    @Mock
//    private InMemoryLinkRepository linkRepository;
//
//    @Mock
//    private BotHttpClient botHttpClient;
//
//    private LinkCheckService linkCheckService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        when(botHttpClient.sendUpdate(any(LinkUpdate.class))).thenReturn(Mono.empty());
//        linkCheckService = new LinkCheckService(gitHubClient, linkRepository, botHttpClient);
//    }
//
//    @Test
//    void checkLinks_shouldSendUpdateWhenRepositoryIsUpdated() {
//
//        String url = "https://github.com/user/repo";
//        long chatId = 12345L;
//        Link link = new Link(chatId, url, null, null);
//        GitHubRepository gitHubRepo = new GitHubRepository(1L, "repo", "main", "2025-02-16T12:00:00Z");
//
//        when(linkRepository.findAll()).thenReturn(List.of(link));
//        when(linkRepository.isInitialized(url)).thenReturn(true);
//        when(linkRepository.getLastUpdated(url)).thenReturn(LocalDateTime.of(2025, 2, 15, 10, 0));
//
//        when(gitHubClient.getRepository("user", "repo")).thenReturn(Mono.just(gitHubRepo));
//
//        linkCheckService.checkLinks();
//
//        verify(botHttpClient, times(1)).sendUpdate(any(LinkUpdate.class));
//    }
//
//    @Test
//    void checkLinks_shouldNotSendUpdateWhenRepositoryIsNotUpdated() {
//
//        String url = "https://github.com/user/repo";
//        long chatId = 12345L;
//        Link link = new Link(chatId, url, null, null);
//        GitHubRepository gitHubRepo = new GitHubRepository(1L, "repo", "main", "2025-02-16T12:00:00Z");
//
//        when(linkRepository.findAll()).thenReturn(List.of(link));
//        when(linkRepository.isInitialized(url)).thenReturn(true);
//        when(linkRepository.getLastUpdated(url)).thenReturn(gitHubRepo.getPushedAtAsDateTime()); // Same timestamp
//        when(gitHubClient.getRepository("user", "repo")).thenReturn(Mono.just(gitHubRepo));
//
//        linkCheckService.checkLinks();
//
//        verify(botHttpClient, never()).sendUpdate(any(LinkUpdate.class));
//    }
//
//    @Test
//    void checkLinks_shouldInitializeLinkWhenFirstTime() {
//
//        String url = "https://github.com/user/repo";
//        long chatId = 12345L;
//        Link link = new Link(chatId, url, null, null);
//        GitHubRepository gitHubRepo = new GitHubRepository(1L, "repo", "main", "2025-02-16T12:00:00Z");
//
//        when(linkRepository.findAll()).thenReturn(List.of(link));
//        when(linkRepository.isInitialized(url)).thenReturn(false);
//        when(gitHubClient.getRepository("user", "repo")).thenReturn(Mono.just(gitHubRepo));
//
//        linkCheckService.checkLinks();
//
//        verify(linkRepository, times(1)).markInitialized(url);
//        verify(linkRepository, times(1)).updateLastUpdated(url, gitHubRepo.getPushedAtAsDateTime());
//    }
//}
