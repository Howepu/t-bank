package backend.academy.scrapper.services.github;

import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.github.GitHubIssuePullRequest;
import backend.academy.scrapper.github.GitHubRepository;
import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final Timer ghScrapeTimer;
    private final MeterRegistry registry;

    private static final int BATCH_SIZE = 250;
    private static final int NUM_THREADS = 4;

    private static final String UPDATE_MESSAGE_TEMPLATE = "🔔 Обновления в репозитории на GitHub: **%s**%n"
            + "📅 Последний коммит: %s%n" + "🔗 [Смотреть на GitHub](%s)";

    private static final String ISSUE_PR_UPDATE_TEMPLATE =
            "🔔 Новое %s в репозитории **%s**:%n" + "👤 Автор: %s%n" + "📅 Время создания: %s%n" + "✉️ %s%n";

    private final ExecutorService executorService;

    public AbstractLinkCheckService(
            GitHubClient gitHubClient, LinkService linkService, ChatService chatService, MeterRegistry registry) {
        this.gitHubClient = gitHubClient;
        this.linkService = linkService;
        this.chatService = chatService;
        this.executorService = Executors.newFixedThreadPool(NUM_THREADS);
        this.ghScrapeTimer = Timer.builder("scrape.time")
                .tags("platform", "github")
                .publishPercentiles(0.5, 0.95, 0.90)
                .register(registry);
        this.registry = registry;
    }

    @Scheduled(cron = "${app.cron}")
    public void digestCheckGitHubLinks() {
        log.info("Начинаем проверку ссылок...");

        Set<Chat> chatIds = chatService.getChatsByMode("diajest");
        checkLinks(chatIds);

        log.debug("Найдено {} зарегистрированных чатов для проверки ссылок.", chatIds.size());
    }

    @Scheduled(fixedRate = 5000)
    public void immediateCheckGitHubLinks() {
        log.info("Начинаем проверку ссылок...");

        Set<Chat> chatIds = chatService.getChatsByMode("immediate");
        checkLinks(chatIds);

        log.debug("Найдено {} зарегистрированных чатов для проверки ссылок.", chatIds.size());
    }

    private void checkLinks(Set<Chat> chatIds) {

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

        Timer.Sample sample = Timer.start(registry);
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

        sample.stop(ghScrapeTimer);
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

                boolean skip = false;

                log.info("Фильтры: {}", linkService.getFilterNamesByLink(link.id()));
                for (String filter : linkService.getFilterNamesByLink(link.id())) {
                    log.info("Начинаем поиск фильтров {}", filter);
                    if (isUserFilter(filter)) {
                        String[] parts = filter.split("=");
                        String userName = parts[1];
                        log.info("Имя фильтра: {}", userName);
                        if (userName.equals(issue.gitHubUser().login())) {
                            skip = true;
                            break;
                        }
                    }
                }

                if (!skip) {
                    log.info("Обнаружена новая issue в репозитории {}: Issue ID {}", link.url(), issue.id());
                    sendIssueOrPRUpdate(link, issue, "issue");
                }
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

                boolean skip = false;

                for (String filter : linkService.getFilterNamesByLink(link.id())) {
                    if (isUserFilter(filter)) {
                        String[] parts = filter.split("=");
                        String userName = parts[1];
                        if (Objects.equals(userName, pr.gitHubUser().login())) {
                            log.info("{} в блэк-листе", userName);
                            skip = true;
                            break;
                        }
                    }
                }

                if (!skip) {
                    log.info("Обнаружен новый pull request в репозитории {}: PR ID {}", link.url(), pr.id());
                    sendIssueOrPRUpdate(link, pr, "pull request");
                }
            }
        }

        // Обновляем список известных PRs в БД
        linkService.updateLinkPullRequests(link.id(), currentPrIds);
    }

    protected void sendUpdate(Link link, GitHubRepository gitHubRepository) {
        if (link.chat() == null) {
            log.warn("Чат равен null для ссылки: {}", link.url());
            return;
        }

        String message = String.format(
                UPDATE_MESSAGE_TEMPLATE, gitHubRepository.name(), gitHubRepository.getPushedAtAsDateTime(), link.url());

        LinkUpdate update = new LinkUpdate(
                link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));
        log.info("Отправляем обновление о репозитории в чат {}: {}", link.chat().chatId(), message);
        deliverUpdate(update);
    }

    protected void sendIssueOrPRUpdate(Link link, GitHubIssuePullRequest gitHubIssuePullRequest, String type) {
        if (link.chat() == null) {
            log.warn("Чат равен null для ссылки: {}", link.url());
            return;
        }

        String preview = gitHubIssuePullRequest.body() != null
                        && gitHubIssuePullRequest.body().length() > 200
                ? gitHubIssuePullRequest.body().substring(0, 200)
                : gitHubIssuePullRequest.body();

        String message = String.format(
                ISSUE_PR_UPDATE_TEMPLATE,
                type,
                link.url(),
                gitHubIssuePullRequest.gitHubUser().login(),
                gitHubIssuePullRequest.createdAt(),
                preview);

        LinkUpdate update = new LinkUpdate(
                link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));
        log.info("Отправляем обновление о репозитории в чат {}: {}", link.chat().chatId(), message);
        deliverUpdate(update);
    }

    protected abstract void deliverUpdate(LinkUpdate update);

    private boolean isGitHubRepo(String url) {
        return url != null && url.matches("https://github\\.com/[^/]+/[^/]+");
    }

    private boolean isUserFilter(String str) {
        return str != null && str.matches("user=[a-zA-Z0-9_.]+$");
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
