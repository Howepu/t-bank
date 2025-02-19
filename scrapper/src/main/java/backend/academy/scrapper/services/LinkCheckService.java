// package backend.academy.scrapper.services;
//
// import backend.academy.scrapper.bot.BotHttpClient;
// import backend.academy.scrapper.dto.LinkUpdate;
// import backend.academy.scrapper.github.GitHubClient;
// import backend.academy.scrapper.github.GitHubRepository;
// import backend.academy.scrapper.models.Link;
// import backend.academy.scrapper.repositories.InMemoryLinkRepository;
// import java.util.List;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
//
// @Service
// @Slf4j
// public class LinkCheckService {
//
//    private final GitHubClient gitHubClient;
//    private final InMemoryLinkRepository linkRepository;
//    private final BotHttpClient botHttpClient;
//
//    private static final String UPDATE_MESSAGE_TEMPLATE = "🔔 Обновления в репозитории на GitHub: **%s**%n"
//            + "📅 Последний коммит: %s%n" + "🔗 [Смотреть на GitHub](%s)";
//
//    public LinkCheckService(
//            GitHubClient gitHubClient, InMemoryLinkRepository linkRepository, BotHttpClient botHttpClient) {
//        this.gitHubClient = gitHubClient;
//        this.linkRepository = linkRepository;
//        this.botHttpClient = botHttpClient;
//    }
//
//    @Scheduled(fixedRate = 5000)
//    public void checkLinks() {
//        for (Link link : linkRepository.findAll()) {
//            if (isGitHubRepo(link.url())) {
//                checkGitHubRepo(link);
//            }
//        }
//    }
//
//    private void checkGitHubRepo(Link link) {
//        String[] parts = link.url().split("/");
//        String owner = parts[3];
//        String repo = parts[4];
//
//        gitHubClient
//                .getRepository(owner, repo)
//                .doOnNext(gitHubRepository -> processGitHubRepository(gitHubRepository, link))
//                .subscribe();
//    }
//
//    private void processGitHubRepository(GitHubRepository gitHubRepository, Link link) {
//        if (!linkRepository.isInitialized(link.url())) {
//            if (linkRepository.getLastUpdated(link.url()) == null) {
//                linkRepository.updateLastUpdated(link.url(), gitHubRepository.getPushedAtAsDateTime());
//            }
//            linkRepository.markInitialized(link.url());
//            return;
//        }
//
//        if (gitHubRepository.getPushedAtAsDateTime() != null
//                && !gitHubRepository.getPushedAtAsDateTime().equals(linkRepository.getLastUpdated(link.url()))) {
//            linkRepository.updateLastUpdated(link.url(), gitHubRepository.getPushedAtAsDateTime());
//            sendUpdate(link, gitHubRepository);
//        }
//    }
//
//    private void sendUpdate(Link link, GitHubRepository gitHubRepository) {
//        String message = String.format(
//                UPDATE_MESSAGE_TEMPLATE, gitHubRepository.name(), gitHubRepository.getPushedAtAsDateTime(),
// link.url());
//
//        LinkUpdate update = new LinkUpdate(link.chatId(), link.url(), message, List.of(link.chatId()));
//
//        log.info("Отправка уведомления: {}", message);
//        botHttpClient.sendUpdate(update).subscribe();
//    }
//
//    private boolean isGitHubRepo(String url) {
//        // Проверяем, что URL соответствует формату репозитория на GitHub
//        return url.matches("https://github\\.com/[^/]+/[^/]+");
//    }
// }
