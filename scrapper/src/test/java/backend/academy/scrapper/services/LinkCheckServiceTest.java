// package backend.academy.scrapper.services;
//
// import backend.academy.scrapper.bot.BotHttpClient;
// import backend.academy.scrapper.github.GitHubClient;
// import backend.academy.scrapper.github.GitHubRepository;
// import backend.academy.scrapper.models.Chat;
// import backend.academy.scrapper.models.Link;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.test.context.junit.jupiter.SpringExtension;
// import org.springframework.web.reactive.function.client.WebClient;
// import org.testcontainers.containers.PostgreSQLContainer;
// import org.testcontainers.junit.jupiter.Container;
// import org.testcontainers.junit.jupiter.Testcontainers;
// import reactor.core.publisher.Mono;
//
// import java.time.Instant;
// import java.time.ZoneOffset;
// import java.util.List;
// import java.util.Set;
// import java.util.concurrent.CountDownLatch;
// import java.util.concurrent.TimeUnit;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.*;
//
// @ExtendWith(SpringExtension.class)
// @SpringBootTest
// @Testcontainers
// class LinkCheckServiceTest {
//
//    @Container
//    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine")
//        .withDatabaseName("scrapper")
//        .withUsername("postgres")
//        .withPassword("postgres");
//
//
//    @Autowired
//    private LinkCheckService linkCheckService;
//
//    @Autowired
//    private ChatService chatService;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    private WebClient webClient;
//
//
//    @BeforeEach
//    void cleanDatabase() {
//        jdbcTemplate.execute("TRUNCATE TABLE link_tag, link, chat, tag RESTART IDENTITY CASCADE");
//    }
//
//    @Autowired
//    private LinkService linkService;
//
//    @MockBean
//    private GitHubClient gitHubClient;
//
//    @MockBean
//    private BotHttpClient botHttpClient;
//
//    private final Long chatId = 123L;
//
//    @Test
//    void checkLinks_ShouldProcessGitHubRepo() {
//        // Создаем и сохраняем чат
//        Chat chat = new Chat(chatId);
//        chatService.registerChat(chat.chatId());
//
//        // Добавляем ссылку на GitHub
//        Link link = new Link("https://github.com/test/repo", chat);
//        linkService.addLink(chatId, link);
//
//        // Заглушка для ответа от GitHub API
//        when(gitHubClient.getRepository("test", "repo"))
//            .thenReturn(Mono.just(new GitHubRepository(
//                123L,             // id (любое значение)
//                "repo",          // name
//                "main",          // default_branch
//                "2025-03-26T12:00:00Z" // pushed_at
//            )));
//
//
//        when(botHttpClient.sendUpdate(any())).thenReturn(Mono.empty());
//
//        // Запускаем проверку ссылок
//        linkCheckService.checkLinks();
//
//        // Проверяем, что обновление отправилось
//        verify(botHttpClient, timeout(5000)).sendUpdate(any());
//    }
//
//    @Test
//    void checkLinks_ShouldNotSendUpdateIfNoChanges() {
//        Chat chat = new Chat(chatId);
//        chatService.registerChat(chat.chatId());
//
//        Link link = new Link("https://github.com/test/repo", chat);
//        linkService.addLink(chatId, link);
//
//        linkService.updateLastUpdated(
//            link.id(),
//            Instant.parse("2025-03-25T12:00:00Z").atZone(ZoneOffset.UTC).toLocalDateTime()
//        );
//
//        when(gitHubClient.getRepository("test", "repo"))
//            .thenReturn(Mono.just(new GitHubRepository(
//                123L,             // id (любое значение)
//                "repo",          // name
//                "main",          // default_branch
//                "2025-03-26T12:00:00Z" // pushed_at
//            )));
//
//
//        linkCheckService.checkLinks();
//
//        verify(botHttpClient, never()).sendUpdate(any());
//    }
//
//
//    @Test
//    void checkLinks_ShouldSendUpdateOnNewCommit() throws InterruptedException {
//        // Подготовка
//        Chat chat = new Chat(chatId);
//        chatService.registerChat(chat.chatId());
//
//        Link link = new Link("https://github.com/test/repo", chat);
//        linkService.addLink(chatId, link);
//
//        linkService.updateLastUpdated(
//            link.id(),
//            Instant.parse("2025-03-25T12:00:00Z").atZone(ZoneOffset.UTC).toLocalDateTime()
//        );
//
//        when(gitHubClient.getRepository("test", "repo"))
//            .thenReturn(Mono.just(new GitHubRepository(
//                123L,
//                "repo",
//                "main",
//                "2025-03-26T12:00:00Z"
//            )));
//
//        when(botHttpClient.sendUpdate(any())).thenReturn(Mono.empty());
//
//        // Создаем CountDownLatch для ожидания завершения асинхронных задач
//        CountDownLatch latch = new CountDownLatch(1);
//        // Добавляем Listener для отслеживания завершения
//        when(botHttpClient.sendUpdate(any()))
//            .thenReturn(Mono.fromRunnable(latch::countDown));
//
//        // Выполнение
//        linkCheckService.checkLinks();
//
//        // Ждем завершения, либо тайм-аут
//        latch.await(5, TimeUnit.SECONDS);
//
//        // Проверяем, что sendUpdate был вызван
//        verify(botHttpClient, times(1)).sendUpdate(any());
//    }
//
// }
