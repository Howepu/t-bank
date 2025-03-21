package backend.academy.scrapper.services;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.github.GitHubIssuePullRequest;
import backend.academy.scrapper.github.GitHubRepository;
import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public LinkCheckService(
        GitHubClient gitHubClient,
        LinkService linkService,
        BotHttpClient botHttpClient,
        ChatService chatService) {
        this.gitHubClient = gitHubClient;
        this.linkService = linkService;
        this.botHttpClient = botHttpClient;
        this.chatService = chatService;
    }

    @Scheduled(fixedRate = 5000)
    public void checkLinks() {
        log.info("Starting link check...");

        Set<Chat> chatIds = chatService.getAllRegisteredChatIds();
        for (Chat chat : chatIds) {
            List<Link> links = linkService.getAllLinks(chat.chatId());
            for (Link link : links) {
                if (isGitHubRepo(link.url())) {
                    log.info(link.url());
                    checkGitHubRepo(link);
                }
            }
        }
        log.info("Finished link check.");
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
        boolean updated = false;
        List<Long> knownIssueIds = link.knownIssueIds() != null ? link.knownIssueIds() : new ArrayList<>();
        link.knownIssueIds(knownIssueIds);

        for (GitHubIssuePullRequest issue : issues) {
            if (!knownIssueIds.contains(issue.id())) {
                knownIssueIds.add(issue.id());
                sendIssueOrPRUpdate(link, issue, "Issue");
                updated = true;
            }
        }

        if (updated) {
            linkService.addLink(link.chat().chatId(), link);
        }
    }

    private void processGitHubPullRequests(Link link, List<GitHubIssuePullRequest> prs) {
        boolean updated = false;
        List<Long> knownPrIds = link.knownPrIds() != null ? link.knownPrIds() : new ArrayList<>();
        link.knownPrIds(knownPrIds);

        for (GitHubIssuePullRequest pr : prs) {
            if (!knownPrIds.contains(pr.id())) {
                knownPrIds.add(pr.id());
                sendIssueOrPRUpdate(link, pr, "Pull Request");
                updated = true;
            }
        }

        if (updated) {
            linkService.addLink(link.chat().chatId(), link);
        }
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
}
