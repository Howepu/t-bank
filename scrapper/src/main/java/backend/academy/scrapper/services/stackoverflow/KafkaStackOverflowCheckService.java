package backend.academy.scrapper.services.stackoverflow;

import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.stackoverflow.StackOverflowClient;
import backend.academy.scrapper.stackoverflow.StackOverflowQuestionAnswer;
import java.util.List;
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
            KafkaTemplate<String, LinkUpdate> kafkaTemplate) {
        super(stackOverflowClient, linkService, chatService);
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    protected void sendUpdate(Link link, StackOverflowQuestionAnswer stackOverflowQuestion) {
        String title = stackOverflowQuestion.title() != null ? stackOverflowQuestion.title() : "Без названия";
        String lastActivity = stackOverflowQuestion.getLastActivityDateAsDateTime() != null
                ? stackOverflowQuestion.getLastActivityDateAsDateTime().toString()
                : "Не указано время";

        String message = String.format(QUESTION_UPDATE_MESSAGE_TEMPLATE, title, lastActivity, link.url());

        LinkUpdate update = new LinkUpdate(
                link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));

        log.info("Отправка уведомления: {}", message);
        kafkaTemplate.send(updates, update);
    }

    @Override
    protected void sendAnswerUpdate(Link link, StackOverflowQuestionAnswer answer) {
        String title = answer.title() != null ? answer.title() : "Без названия";
        String displayName = answer.owner().name();
        String creationDate =
            answer.getCreationDate() != null ? answer.getCreationDate().toString() : "Не указано время";

        String message = String.format(ANSWER_UPDATE_MESSAGE_TEMPLATE, title, displayName, creationDate, link.url());

        LinkUpdate update = new LinkUpdate(
            link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));

        log.info("Отправка обновления ответа: {}", message);
        kafkaTemplate.send(updates, update);
    }
}
