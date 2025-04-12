package backend.academy.scrapper.services.github;

import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.github.GitHubIssuePullRequest;
import backend.academy.scrapper.github.GitHubRepository;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class KafkaLinkCheckService extends AbstractLinkCheckService {

    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;
    private static final String UPDATE_MESSAGE_TEMPLATE = "🔔 Обновления в репозитории на GitHub: **%s**%n"
            + "📅 Последний коммит: %s%n" + "🔗 [Смотреть на GitHub](%s)";

    private static final String ISSUE_PR_UPDATE_TEMPLATE =
            "🔔 Новое %s в репозитории **%s**:%n" + "👤 Автор: %s%n" + "📅 Время создания: %s%n" + "✉️ %s%n";

    @Value("app.kafka-topic-updates")
    private String updates;

    public KafkaLinkCheckService(
            GitHubClient gitHubClient,
            LinkService linkService,
            ChatService chatService,
            KafkaTemplate<String, LinkUpdate> kafkaTemplate,
            String updates) {
        super(gitHubClient, linkService, chatService);
        this.kafkaTemplate = kafkaTemplate;
        this.updates = updates;
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
        kafkaTemplate.send(updates, update);
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
        kafkaTemplate.send(updates, update);
    }
}
