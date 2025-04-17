package backend.academy.scrapper.services.github;

import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class KafkaLinkCheckService extends AbstractLinkCheckService {

    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;

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
    protected void deliverUpdate(LinkUpdate update) {
        kafkaTemplate.send(updates, update);
    }
}
