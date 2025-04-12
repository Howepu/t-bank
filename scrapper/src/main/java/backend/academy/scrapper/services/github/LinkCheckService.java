package backend.academy.scrapper.services.github;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.github.GitHubIssuePullRequest;
import backend.academy.scrapper.github.GitHubRepository;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LinkCheckService extends AbstractLinkCheckService {

    private final BotHttpClient botHttpClient;

    private static final String UPDATE_MESSAGE_TEMPLATE = "🔔 Обновления в репозитории на GitHub: **%s**%n"
            + "📅 Последний коммит: %s%n" + "🔗 [Смотреть на GitHub](%s)";

    private static final String ISSUE_PR_UPDATE_TEMPLATE =
            "🔔 Новое %s в репозитории **%s**:%n" + "👤 Автор: %s%n" + "📅 Время создания: %s%n" + "✉️ %s%n";

    public LinkCheckService(
            GitHubClient gitHubClient, LinkService linkService, ChatService chatService, BotHttpClient botHttpClient) {
        super(gitHubClient, linkService, chatService);
        this.botHttpClient = botHttpClient;
    }

    @Override
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
        botHttpClient.sendUpdate(update).subscribe();
    }

    @Override
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
                gitHubIssuePullRequest.gitHubUser(),
                gitHubIssuePullRequest.createdAt(),
                preview);

        LinkUpdate update = new LinkUpdate(
                link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));
        log.info("Отправляем обновление о репозитории в чат {}: {}", link.chat().chatId(), message);
        botHttpClient.sendUpdate(update).subscribe();
    }
}
