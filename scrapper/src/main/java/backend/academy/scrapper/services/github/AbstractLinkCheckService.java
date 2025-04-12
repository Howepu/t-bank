package backend.academy.scrapper.services.github;

import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.github.GitHubIssuePullRequest;
import backend.academy.scrapper.github.GitHubRepository;
import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
public abstract class AbstractLinkCheckService {

    private final GitHubClient gitHubClient;
    private final LinkService linkService;
    private final ChatService chatService;

    private static final int BATCH_SIZE = 250;
    private static final int NUM_THREADS = 4;

    private final ExecutorService executorService;

    public AbstractLinkCheckService(GitHubClient gitHubClient, LinkService linkService, ChatService chatService) {
        this.gitHubClient = gitHubClient;
        this.linkService = linkService;
        this.chatService = chatService;
        this.executorService = Executors.newFixedThreadPool(NUM_THREADS);
    }

    @Scheduled(fixedRate = 5000)
    public void checkLinks() {
        log.info("Начинаем проверку ссылок...");

        Set<Chat> chatIds = chatService.getAllRegisteredChatIds();
        log.debug("Найдено {} зарегистрированных чатов для проверки ссылок.", chatIds.size());

        for (Chat chat : chatIds) {
            List<Link> links = linkService.getAllLinks(chat.chatId());
            log.debug("Найдено {} ссылок для чата {}.", links.size(), chat.chatId());

            // Разделяем ссылки на батчи
            int totalLinks = links.size();
            for (int i = 0; i < totalLinks; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, totalLinks);
                List<Link> batch = links.subList(i, end);

                // Отправляем батч в поток
                log.debug("Обрабатываем батч из {} ссылок для чата {}.", batch.size(), chat.chatId());
                executorService.submit(() -> processBatch(batch));
            }
        }

        log.info("Finished link check.");
    }

    private void processBatch(List<Link> batch) {
        for (Link link : batch) {
            if (isGitHubRepo(link.url())) {
                log.info("Проверяем ссылку на репозиторий GitHub: {}", link.url());
                checkGitHubRepo(link);
            } else {
                log.warn("Пропускаем ссылку, так как она не относится к GitHub: {}", link.url());
            }
        }
    }

    private void checkGitHubRepo(Link link) {
        String[] parts = link.url().split("/");
        String owner = parts[3];
        String repo = parts[4];

        log.info("Проверяем репозиторий GitHub: {}/{}", owner, repo);

        gitHubClient
                .getRepository(owner, repo)
                .doOnNext(repoData -> processGitHubRepository(repoData, link))
                .then(gitHubClient.getIssues(owner, repo).doOnNext(issues -> processGitHubIssues(link, issues)))
                .then(gitHubClient.getPullRequests(owner, repo).doOnNext(prs -> processGitHubPullRequests(link, prs)))
                .subscribe(
                        result -> log.debug("Репозиторий проверен: {}", link.url()),
                        error -> log.error(
                                "Ошибка при проверке репозитория GitHub {}: {}", link.url(), error.getMessage()));
    }

    private void processGitHubRepository(GitHubRepository gitHubRepository, Link link) {
        if (link.createdAt() == null) {
            log.debug("Первый раз проверяем репозиторий, обновляем дату создания.");
            linkService.updateLastUpdated(link.id(), gitHubRepository.getPushedAtAsDateTime());
            return;
        }

        if (gitHubRepository.getPushedAtAsDateTime() != null
                && !gitHubRepository.getPushedAtAsDateTime().equals(link.createdAt())) {
            log.info("Репозиторий {} обновлён. Уведомляем подписчиков.", link.url());
            linkService.updateLastUpdated(link.id(), gitHubRepository.getPushedAtAsDateTime());
            sendUpdate(link, gitHubRepository);
        } else {
            log.debug("Обновлений в репозитории {} нет с момента последней проверки.", link.url());
        }
    }

    private void processGitHubIssues(Link link, List<GitHubIssuePullRequest> issues) {
        List<Long> currentIssueIds = new ArrayList<>();
        for (GitHubIssuePullRequest issue : issues) {
            currentIssueIds.add(issue.id());

            // Проверяем, есть ли этот issue в известных
            if (link.knownIssueIds() == null || !link.knownIssueIds().contains(issue.id())) {
                log.info("Обнаружена новая issue в репозитории {}: Issue ID {}", link.url(), issue.id());
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
                log.info("Обнаружен новый pull request в репозитории {}: PR ID {}", link.url(), pr.id());
                sendIssueOrPRUpdate(link, pr, "pull request");
            }
        }

        // Обновляем список известных PRs в БД
        linkService.updateLinkPullRequests(link.id(), currentPrIds);
    }

    protected abstract void sendUpdate(Link link, GitHubRepository gitHubRepository);

    protected abstract void sendIssueOrPRUpdate(Link link, GitHubIssuePullRequest gitHubIssuePullRequest, String type);

    private boolean isGitHubRepo(String url) {
        return url != null && url.matches("https://github\\.com/[^/]+/[^/]+");
    }

    @PreDestroy
    public void shutdownExecutor() throws InterruptedException {
        log.info("Shutting down executor service...");
        executorService.shutdown();
        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
            log.warn("Executor service did not terminate in time, forcing shutdown...");
            executorService.shutdownNow();
        }
    }
}
