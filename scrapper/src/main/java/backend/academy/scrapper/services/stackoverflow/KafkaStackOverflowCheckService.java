package backend.academy.scrapper.services.stackoverflow;

import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.stackoverflow.StackOverflowClient;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public class KafkaStackOverflowCheckService extends AbstractStackoverflowCheckService {

    private static final String QUESTION_UPDATE_MESSAGE_TEMPLATE = "🔔 Обновления в вопросе на StackOverflow: **%s**%n"
            + "📅 Последняя активность: %s%n" + "🔗 [Смотреть на StackOverflow](%s)";

    private static final String ANSWER_UPDATE_MESSAGE_TEMPLATE = "🔔 Новый ответ на вопрос **%s** на StackOverflow:%n"
            + "👤 Автор: %s%n" + "📅 Время ответа: %s%n" + "🔗 [Смотреть ответ на StackOverflow](%s)";

    @Value("app.kafka-topic-updates")
    private String updates;

    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;

    public KafkaStackOverflowCheckService(
            StackOverflowClient stackOverflowClient,
            LinkService linkService,
            ChatService chatService,
            KafkaTemplate<String, LinkUpdate> kafkaTemplate,
            MeterRegistry registry) {
        super(stackOverflowClient, linkService, chatService, registry);
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    protected void deliverUpdate(LinkUpdate update) {
        kafkaTemplate.send(updates, update);
    }
}
