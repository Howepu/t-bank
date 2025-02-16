package backend.academy.scrapper.services;

import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.repositories.InMemoryLinkRepository;
import backend.academy.scrapper.repositories.LinkRepository;
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
    private final InMemoryLinkRepository linkRepository; // Используем правильный репозиторий
    private final BotHttpClient botHttpClient;

    public LinkCheckService(GitHubClient gitHubClient, InMemoryLinkRepository linkRepository,
                            BotHttpClient botHttpClient) {
        this.gitHubClient = gitHubClient;
        this.linkRepository = linkRepository;
        this.botHttpClient = botHttpClient;
    }

    @Scheduled(fixedRate = 5000) // Проверка раз в минуту
    public void checkLinks() {
        // Получаем все ссылки из репозитория
        for (Link link : linkRepository.findAll()) {
            if (isGitHubRepo(link.url())) {
                checkGitHubRepo(link);
            }
        }
    }

    private void checkGitHubRepo(Link link) {
        String[] parts = link.url().split("/");
        String owner = parts[3];
        System.out.println(owner);
        String repo = parts[4];
        System.out.println(repo);

        gitHubClient.getRepository(owner, repo)
            .subscribe(gitHubRepo -> {
                if (gitHubRepo.updatedAt() != null && !gitHubRepo.updatedAt().equals(linkRepository.getLastUpdated(link.url()))) {
                    linkRepository.updateLastUpdated(link.url());
                    sendUpdate(link);
                }
            });
    }

    private void sendUpdate(Link link) {

        LinkUpdate update = new LinkUpdate(
            link.chatId(),
            link.url(),
            "Обновления в репозитории: " + link.url(),
            List.of(link.chatId()) // Список chatId для уведомлений
        );

        // Отправляем обновление через BotHttpClient
        botHttpClient.sendUpdate(update).subscribe();
    }

    private boolean isGitHubRepo(String url) {
        // Проверяем, что URL соответствует формату GitHub репозитория
        return url.matches("https://github\\.com/[^/]+/[^/]+");
    }
}
