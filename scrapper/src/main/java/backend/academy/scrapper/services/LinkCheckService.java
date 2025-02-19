package backend.academy.scrapper.services;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.github.GitHubRepository;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.repositories.InMemoryLinkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LinkCheckService {

    private static final String GITHUB_REPO_URL_PATTERN = "https://github\\.com/[^/]+/[^/]+";
    private static final String UPDATE_MESSAGE_TEMPLATE =
            "🔔 Обновления в репозитории **%s**%n" + "📅 Последний коммит: %s%n" + "🔗 [Смотреть на GitHub](%s)";
    private final GitHubClient gitHubClient;
    private final InMemoryLinkRepository linkRepository;
    private final BotHttpClient botHttpClient;

    public LinkCheckService(
            GitHubClient gitHubClient, InMemoryLinkRepository linkRepository, BotHttpClient botHttpClient) {
        this.gitHubClient = gitHubClient;
        this.linkRepository = linkRepository;
        this.botHttpClient = botHttpClient;
    }

    @Scheduled(fixedRate = 5000)
    public void checkLinks() {
        for (Link link : linkRepository.findAll()) {
            if (isGitHubRepo(link.url())) {
                log.info("Проверка репозитория: {}", link.url());
                checkGitHubRepo(link);
            }
        }
    }

    private void checkGitHubRepo(Link link) {
        String[] parts = link.url().split("/");
        String owner = parts[3];
        String repo = parts[4];

        // Используем анонимный класс вместо лямбды
        gitHubClient.getRepository(owner, repo).subscribe(new Consumer<GitHubRepository>() {
            @Override
            public void accept(GitHubRepository gitHubRepo) {
                log.info("Проверка репозитория: {}", link.url());

                // Теперь используем метод getPushedAtAsDateTime() из GitHubRepository
                LocalDateTime pushedAt = gitHubRepo.getPushedAtAsDateTime();

                log.info("Последнее обновление в репозитории: {}", pushedAt);
                log.info("Последнее сохраненное обновление: {}", linkRepository.getLastUpdated(link.url()));

                if (!linkRepository.isInitialized(link.url())) {
                    if (linkRepository.getLastUpdated(link.url()) == null) {
                        linkRepository.updateLastUpdated(link.url(), pushedAt);
                    }
                    linkRepository.markInitialized(link.url());
                    return;
                }

                if (pushedAt != null && linkRepository.getLastUpdated(link.url()) != null) {
                    LocalDateTime lastUpdated = linkRepository.getLastUpdated(link.url());

                    if (pushedAt.isAfter(lastUpdated)) {
                        log.info("Обнаружено новое обновление для репозитория: {}", link.url());
                        linkRepository.updateLastUpdated(link.url(), pushedAt);
                        sendUpdate(link, gitHubRepo);
                    }
                }
            }
        });
    }


    private void sendUpdate(Link link, GitHubRepository gitHubRepo) {
        String message = String.format(
                UPDATE_MESSAGE_TEMPLATE, gitHubRepo.name(), gitHubRepo.getPushedAtAsDateTime(), link.url());

        LinkUpdate update = new LinkUpdate(link.chatId(), link.url(), message, List.of(link.chatId()));

        log.info("Отправка уведомления: {}", message);
        botHttpClient.sendUpdate(update).subscribe();
    }

    private boolean isGitHubRepo(String url) {
        // Проверяем, что URL соответствует формату GitHub репозитория
        return url.matches(GITHUB_REPO_URL_PATTERN);
    }
}
