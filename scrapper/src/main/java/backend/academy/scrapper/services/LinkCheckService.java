package backend.academy.scrapper.services;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.github.GitHubIssuePullRequest;
import backend.academy.scrapper.github.GitHubRepository;
import backend.academy.scrapper.models.Link;
import java.util.ArrayList;
import java.util.List;
import backend.academy.scrapper.repositories.jpa.JpaLinkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class LinkCheckService {

    private final GitHubClient gitHubClient;
    private final JpaLinkRepository linkRepository;
    private final BotHttpClient botHttpClient;

    private static final String UPDATE_MESSAGE_TEMPLATE = "🔔 Обновления в репозитории на GitHub: **%s**%n"
        + "📅 Последний коммит: %s%n" + "🔗 [Смотреть на GitHub](%s)";

    private static final String ISSUE_PR_UPDATE_TEMPLATE = "🔔 Новое %s в репозитории **%s**:%n"
        + "👤 Автор: %s%n" + "📅 Время создания: %s%n" + "✉️ %s%n";

    public LinkCheckService(
        GitHubClient gitHubClient, JpaLinkRepository linkRepository, BotHttpClient botHttpClient) {
        this.gitHubClient = gitHubClient;
        this.linkRepository = linkRepository;
        this.botHttpClient = botHttpClient;
    }

    @Scheduled(fixedRate = 5000)
    public void checkLinks() {
        for (Link link : linkRepository.findAll()) {
            if (isGitHubRepo(link.url())) {
                checkGitHubRepo(link);
            }
        }
    }

    private void checkGitHubRepo(Link link) {
        String[] parts = link.url().split("/");
        String owner = parts[3];
        String repo = parts[4];

        log.info("gitHubClient class: {}", gitHubClient.getClass().getName());

        Mono<GitHubRepository> repoMono = gitHubClient.getRepository(owner, repo);

        Mono<List<GitHubIssuePullRequest>> issueMono = (Mono<List<GitHubIssuePullRequest>>) gitHubClient.getIssues(owner, repo); // Получаем список Issues
        Mono<List<GitHubIssuePullRequest>> pullRequestMono = gitHubClient.getPullRequests(owner, repo); // Получаем список PR

        repoMono.doOnNext(gitHubRepository -> processGitHubRepository(gitHubRepository, link)).subscribe();
        issueMono.doOnNext(gitHubIssuePullRequests -> processGitHubIssues(link, gitHubIssuePullRequests)).subscribe();
        pullRequestMono.doOnNext(gitHubIssuePullRequests -> processGitHubPullRequests(link, gitHubIssuePullRequests)).subscribe();
    }

    private void processGitHubRepository(GitHubRepository gitHubRepository, Link link) {

        // Если ссылка еще не инициализирована (например, она только что добавлена в базу данных)
        if (link.createdAt() == null) {
            // Обновляем дату созданию ссылки в базе данных
            linkRepository.updateCreatedAtById(link.id(), gitHubRepository.getPushedAtAsDateTime());
            return; // Возвращаем управление, чтобы не продолжать выполнение функции
        }

        // Если время пуша изменилось, обновляем и отправляем уведомление
        if (gitHubRepository.getPushedAtAsDateTime() != null
            && !gitHubRepository.getPushedAtAsDateTime().equals(link.createdAt())) {
            linkRepository.updateCreatedAtById(link.id(), gitHubRepository.getPushedAtAsDateTime());
            sendUpdate(link, gitHubRepository);
        }
    }

    private void processGitHubIssues(Link link, List<GitHubIssuePullRequest> issues) {
        boolean updated = false;

        // Проверка на null и инициализация, если нужно
        List<Long> knownIssueIds = link.knownIssueIds();
        if (knownIssueIds == null) {
            knownIssueIds = new ArrayList<>(); // Инициализация пустым списком, если он был null
            link.knownIssueIds(knownIssueIds); // Сохранение измененного списка в объект
        }

        // Проверка новых Issue
        for (GitHubIssuePullRequest issue : issues) {
            // Если Issue еще не добавлено в известные, добавляем и отправляем уведомление
            if (!knownIssueIds.contains(issue.id())) {
                knownIssueIds.add(issue.id()); // Добавляем Issue в список известных
                sendIssueOrPRUpdate(link, issue); // Отправка уведомления о новом Issue
                updated = true;
            }
        }

        // Обновление записи в БД, если были изменения
        if (updated) {
            linkRepository.save(link);
        }
    }


    private void processGitHubPullRequests(Link link, List<GitHubIssuePullRequest> prs) {
        boolean updated = false;

        // Проверка на null и инициализация, если нужно
        List<Long> knownPrIds = link.knownPrIds();
        if (knownPrIds == null) {
            knownPrIds = new ArrayList<>(); // Инициализация пустым списком, если он был null
            link.knownPrIds(knownPrIds); // Сохранение измененного списка в объект
        }

        // Проверка новых PR
        for (GitHubIssuePullRequest pr : prs) {
            // Если PR еще не добавлен в известные, добавляем и отправляем уведомление
            if (!knownPrIds.contains(pr.id())) {
                knownPrIds.add(pr.id()); // Добавляем PR в список известных
                sendIssueOrPRUpdate(link, pr); // Отправка уведомления о новом PR
                updated = true;
            }
        }

        // Обновление записи в БД, если были изменения
        if (updated) {
            linkRepository.save(link);
        }
    }


    private void sendUpdate(Link link, GitHubRepository gitHubRepository) {
        String message = String.format(
            UPDATE_MESSAGE_TEMPLATE, gitHubRepository.name(), gitHubRepository.getPushedAtAsDateTime(), link.url());

        LinkUpdate update = new LinkUpdate(link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));

        log.info("Отправка уведомления: {}", message);
        botHttpClient.sendUpdate(update).subscribe();
    }

    private void sendIssueOrPRUpdate(Link link, GitHubIssuePullRequest gitHubIssuePullRequest) {
        String message = String.format(
            ISSUE_PR_UPDATE_TEMPLATE, gitHubIssuePullRequest.title(), link.url(),
            gitHubIssuePullRequest.gitHubUser(), gitHubIssuePullRequest.createdAt(), gitHubIssuePullRequest.body());

        LinkUpdate update = new LinkUpdate(link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));

        log.info("Отправка уведомления: {}", message);
        botHttpClient.sendUpdate(update).subscribe();
    }

    private boolean isGitHubRepo(String url) {
        // Проверяем, что URL соответствует формату репозитория на GitHub
        return url.matches("https://github\\.com/[^/]+/[^/]+");
    }
}
