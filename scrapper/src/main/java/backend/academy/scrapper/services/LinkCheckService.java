package backend.academy.scrapper.services;

import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.github.GitHubRepository;
import backend.academy.scrapper.repositories.InMemoryLinkRepository;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class LinkCheckService {

    private final GitHubClient gitHubClient;
    private final InMemoryLinkRepository linkRepository;
    private final BotHttpClient botHttpClient;

    public LinkCheckService(GitHubClient gitHubClient, InMemoryLinkRepository linkRepository,
                            BotHttpClient botHttpClient) {
        this.gitHubClient = gitHubClient;
        this.linkRepository = linkRepository;
        this.botHttpClient = botHttpClient;
    }

    @Scheduled(fixedRate = 5000) // Проверка раз в минуту
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

        gitHubClient.getRepository(owner, repo)
            .subscribe(gitHubRepo -> {

                if (!linkRepository.isInitialized(link.url())) {
                    linkRepository.updateLastUpdated(link.url(), gitHubRepo.getPushedAtAsDateTime());
                    linkRepository.markInitialized(link.url());
                    return;
                }

                if (gitHubRepo.pushedAt() != null && !gitHubRepo.getPushedAtAsDateTime()
                    .equals(linkRepository.getLastUpdated(link.url()))) {
                    linkRepository.updateLastUpdated(link.url(), gitHubRepo.getPushedAtAsDateTime());
                    sendUpdate(link, gitHubRepo);
                }
            });
    }

    private void sendUpdate(Link link, GitHubRepository gitHubRepo) {
        String message = String.format(
            "🔔 Обновления в репозитории **%s**\n" +
                "📅 Последний коммит: %s\n" +
                "🔗 [Смотреть на GitHub](%s)",
            gitHubRepo.name(),
            gitHubRepo.getPushedAtAsDateTime(),
            link.url()
        );

        LinkUpdate update = new LinkUpdate(
            link.chatId(),
            link.url(),
            message,
            List.of(link.chatId())
        );

        log.info("Отправка уведомления: {}", message);
        botHttpClient.sendUpdate(update).subscribe();
    }

    private boolean isGitHubRepo(String url) {
        // Проверяем, что URL соответствует формату GitHub репозитория
        return url.matches("https://github\\.com/[^/]+/[^/]+");
    }
}
