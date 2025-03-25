package backend.academy.scrapper.services;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.github.GitHubIssuePullRequest;
import backend.academy.scrapper.github.GitHubRepository;
import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LinkCheckService {

    private final GitHubClient gitHubClient;
    private final LinkService linkService;
    private final BotHttpClient botHttpClient;
    private final ChatService chatService;

    private static final String UPDATE_MESSAGE_TEMPLATE = "🔔 Обновления в репозитории на GitHub: **%s**%n"
        + "📅 Последний коммит: %s%n" + "🔗 [Смотреть на GitHub](%s)";

    private static final String ISSUE_PR_UPDATE_TEMPLATE = "🔔 Новое %s в репозитории **%s**:%n"
        + "👤 Автор: %s%n" + "📅 Время создания: %s%n" + "✉️ %s%n";

    private static final int BATCH_SIZE = 250;
    private static final int NUM_THREADS = 4;

    private final ExecutorService executorService;

    public LinkCheckService(
        GitHubClient gitHubClient,
        LinkService linkService,
        BotHttpClient botHttpClient,
        ChatService chatService) {
        this.gitHubClient = gitHubClient;
        this.linkService = linkService;
        this.botHttpClient = botHttpClient;
        this.chatService = chatService;
        this.executorService = Executors.newFixedThreadPool(NUM_THREADS);
    }

    @Scheduled(fixedRate = 5000)
    public void checkLinks() {
        log.info("Starting link check...");

        Set<Chat> chatIds = chatService.getAllRegisteredChatIds();
        for (Chat chat : chatIds) {
            List<Link> links = linkService.getAllLinks(chat.chatId());

            // Разделяем ссылки на батчи
            int totalLinks = links.size();
            for (int i = 0; i < totalLinks; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, totalLinks);
                List<Link> batch = links.subList(i, end);

                // Отправляем батч в поток
                executorService.submit(() -> processBatch(batch));
            }
        }

        log.info("Finished link check.");
    }

    private void processBatch(List<Link> batch) {
        for (Link link : batch) {
            if (isGitHubRepo(link.url())) {
                log.info(link.url());
                checkGitHubRepo(link);
            }
        }
    }

    private void checkGitHubRepo(Link link) {
        String[] parts = link.url().split("/");
        String owner = parts[3];
        String repo = parts[4];

        log.info("Checking GitHub repo: {}/{}", owner, repo);

        gitHubClient.getRepository(owner, repo)
            .doOnNext(repoData -> processGitHubRepository(repoData, link))
            .then(gitHubClient.getIssues(owner, repo)
                .doOnNext(issues -> processGitHubIssues(link, issues)))
            .then(gitHubClient.getPullRequests(owner, repo)
                .doOnNext(prs -> processGitHubPullRequests(link, prs)))
            .subscribe(
                result -> log.info("Processed GitHub repo: {}", link.url()),
                error -> log.error("Error checking repo {}: {}", link.url(), error.getMessage())
            );
    }

    private void processGitHubRepository(GitHubRepository gitHubRepository, Link link) {
        if (link.createdAt() == null) {
            linkService.updateLastUpdated(link.id(), gitHubRepository.getPushedAtAsDateTime());
            return;
        }

        if (gitHubRepository.getPushedAtAsDateTime() != null
            && !gitHubRepository.getPushedAtAsDateTime().equals(link.createdAt())) {
            linkService.updateLastUpdated(link.id(), gitHubRepository.getPushedAtAsDateTime());
            sendUpdate(link, gitHubRepository);
        }
    }

    private void processGitHubIssues(Link link, List<GitHubIssuePullRequest> issues) {
        List<Long> currentIssueIds = new ArrayList<>();
        for (GitHubIssuePullRequest issue : issues) {
            currentIssueIds.add(issue.id());

            // Проверяем, есть ли этот issue в известных
            if (link.knownIssueIds() == null || !link.knownIssueIds().contains(issue.id())) {
                sendIssueOrPRUpdate(link, issue, "issue");
            }
        }

        // Обновляем список известных issues в БД
        linkService.updateLinkIssues(link.id(), currentIssueIds);
    }

    private void processGitHubPullRequests(Link link, List<GitHubIssuePullRequest> prs) {
        List<Long> currentPrIds = new ArrayList<>();
        for (GitHubIssuePullRequest pr : prs) {
            currentPrIds.add(pr.id());

            // Проверяем, есть ли этот PR в известных
            if (link.knownPrIds() == null || !link.knownPrIds().contains(pr.id())) {
                sendIssueOrPRUpdate(link, pr, "pull request");
            }
        }

        // Обновляем список известных PRs в БД
        linkService.updateLinkPullRequests(link.id(), currentPrIds);
    }

    private void sendUpdate(Link link, GitHubRepository gitHubRepository) {
        if (link.chat() == null) {
            log.warn("Chat is null for link: {}", link.url());
            return;
        }

        String message = String.format(
            UPDATE_MESSAGE_TEMPLATE,
            gitHubRepository.name(),
            gitHubRepository.getPushedAtAsDateTime(),
            link.url()
        );

        LinkUpdate update = new LinkUpdate(link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));
        log.info("Sending update: {}", message);
        botHttpClient.sendUpdate(update).subscribe();
    }

    private void sendIssueOrPRUpdate(Link link, GitHubIssuePullRequest gitHubIssuePullRequest, String type) {
        if (link.chat() == null) {
            log.warn("Chat is null for link: {}", link.url());
            return;
        }

        String preview = gitHubIssuePullRequest.body() != null && gitHubIssuePullRequest.body().length() > 200
            ? gitHubIssuePullRequest.body().substring(0, 200)
            : gitHubIssuePullRequest.body();

        String message = String.format(
            ISSUE_PR_UPDATE_TEMPLATE,
            type,
            link.url(),
            gitHubIssuePullRequest.gitHubUser(),
            gitHubIssuePullRequest.createdAt(),
            preview
        );

        LinkUpdate update = new LinkUpdate(link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));
        log.info("Sending {} update: {}", type, message);
        botHttpClient.sendUpdate(update).subscribe();
    }

    private boolean isGitHubRepo(String url) {
        return url != null && url.matches("https://github\\.com/[^/]+/[^/]+");
    }

    @PreDestroy
    public void shutdownExecutor() throws InterruptedException {
        executorService.shutdown();
        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
            executorService.shutdownNow();
        }
    }
}
