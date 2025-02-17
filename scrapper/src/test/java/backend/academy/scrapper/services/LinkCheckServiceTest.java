package backend.academy.scrapper.services;

import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.github.GitHubRepository;
import backend.academy.scrapper.repositories.InMemoryLinkRepository;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

class LinkCheckServiceTest {

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private InMemoryLinkRepository linkRepository;

    @Mock
    private BotHttpClient botHttpClient;

    private LinkCheckService linkCheckService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(botHttpClient.sendUpdate(any(LinkUpdate.class))).thenReturn(Mono.empty());
        linkCheckService = new LinkCheckService(gitHubClient, linkRepository, botHttpClient);
    }

    @Test
    void checkLinks_shouldSendUpdateWhenRepositoryIsUpdated() {

        String url = "https://github.com/user/repo";
        long chatId = 12345L;
        Link link = new Link(chatId, url, null, null);
        GitHubRepository gitHubRepo = new GitHubRepository(1L, "repo", "main", "2025-02-16T12:00:00Z");

        // Симулируем, что последняя дата обновления репозитория была вчера
        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkRepository.isInitialized(url)).thenReturn(true);
        when(linkRepository.getLastUpdated(url)).thenReturn(LocalDateTime.now().plusDays(1));  // Дата обновления + 1 день
        when(gitHubClient.getRepository("user", "repo")).thenReturn(Mono.just(gitHubRepo));

        // When
        linkCheckService.checkLinks();

        // Then
        // Проверяем, что метод отправки обновления был вызван
        verify(botHttpClient, times(1)).sendUpdate(any(LinkUpdate.class));
    }

    @Test
    void checkLinks_shouldNotSendUpdateWhenRepositoryIsNotUpdated() {

        String url = "https://github.com/user/repo";
        long chatId = 12345L;
        Link link = new Link(chatId, url, null, null);
        GitHubRepository gitHubRepo = new GitHubRepository(1L, "repo", "main", "2025-02-16T12:00:00Z");

        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkRepository.isInitialized(url)).thenReturn(true);
        when(linkRepository.getLastUpdated(url)).thenReturn(gitHubRepo.getPushedAtAsDateTime());  // Same timestamp
        when(gitHubClient.getRepository("user", "repo")).thenReturn(Mono.just(gitHubRepo));

        linkCheckService.checkLinks();

        verify(botHttpClient, never()).sendUpdate(any(LinkUpdate.class));
    }

    @Test
    void checkLinks_shouldInitializeLinkWhenFirstTime() {

        String url = "https://github.com/user/repo";
        long chatId = 12345L;
        Link link = new Link(chatId, url, null, null);
        GitHubRepository gitHubRepo = new GitHubRepository(1L, "repo", "main", "2025-02-16T12:00:00Z");

        when(linkRepository.findAll()).thenReturn(List.of(link));
        when(linkRepository.isInitialized(url)).thenReturn(false);  // Link not initialized
        when(gitHubClient.getRepository("user", "repo")).thenReturn(Mono.just(gitHubRepo));

        linkCheckService.checkLinks();

        verify(linkRepository, times(1)).markInitialized(url);
        verify(linkRepository, times(1)).updateLastUpdated(url, gitHubRepo.getPushedAtAsDateTime());
    }
}

