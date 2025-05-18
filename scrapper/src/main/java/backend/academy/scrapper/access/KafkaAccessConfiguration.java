package backend.academy.scrapper.access;

import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.services.github.AbstractLinkCheckService;
import backend.academy.scrapper.services.github.KafkaLinkCheckService;
import backend.academy.scrapper.services.stackoverflow.AbstractStackoverflowCheckService;
import backend.academy.scrapper.services.stackoverflow.KafkaStackOverflowCheckService;
import backend.academy.scrapper.stackoverflow.StackOverflowClient;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "kafka")
public class KafkaAccessConfiguration {

    @Value("app.kafka-topic-updates")
    private String updates;

    @Bean
    public AbstractLinkCheckService linkCheckService(
            GitHubClient gitHubClient,
            LinkService linkService,
            ChatService chatService,
            KafkaTemplate<String, LinkUpdate> kafkaTemplate,
            MeterRegistry registry) {
        return new KafkaLinkCheckService(gitHubClient, linkService, chatService, kafkaTemplate, updates, registry);
    }

    @Bean
    public AbstractStackoverflowCheckService stackoverflowCheckService(
            StackOverflowClient stackOverflowClient,
            LinkService linkService,
            ChatService chatService,
            KafkaTemplate<String, LinkUpdate> kafkaTemplate,
            MeterRegistry registry) {
        return new KafkaStackOverflowCheckService(
                stackOverflowClient, linkService, chatService, kafkaTemplate, registry);
    }
}
