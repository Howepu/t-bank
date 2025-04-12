package backend.academy.scrapper.services.stackoverflow;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.stackoverflow.StackOverflowClient;
import backend.academy.scrapper.stackoverflow.StackOverflowQuestionAnswer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
public class StackOverflowCheckService extends AbstractStackoverflowCheckService {

    private final BotHttpClient botHttpClient;

    private static final String QUESTION_UPDATE_MESSAGE_TEMPLATE = "🔔 Обновления в вопросе на StackOverflow: **%s**%n"
        + "📅 Последняя активность: %s%n" + "🔗 [Смотреть на StackOverflow](%s)";

    private static final String ANSWER_UPDATE_MESSAGE_TEMPLATE = "🔔 Новый ответ на вопрос **%s** на StackOverflow:%n"
        + "👤 Автор: %s%n" + "📅 Время ответа: %s%n" + "🔗 [Смотреть ответ на StackOverflow](%s)";

    public StackOverflowCheckService(StackOverflowClient stackOverflowClient, LinkService linkService,
                                     BotHttpClient botHttpClient, ChatService chatService) {
        super(stackOverflowClient, linkService, chatService);
        this.botHttpClient = botHttpClient;
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
        botHttpClient.sendUpdate(update).subscribe();
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
        botHttpClient.sendUpdate(update).subscribe();
    }
}
