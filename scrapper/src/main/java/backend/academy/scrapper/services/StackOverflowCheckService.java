package backend.academy.scrapper.services;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubIssuePullRequest;
import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.stackoverflow.StackOverflowClient;
import backend.academy.scrapper.stackoverflow.StackOverflowQuestionAnswer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class StackOverflowCheckService {

    private final StackOverflowClient stackOverflowClient;
    private final LinkService linkService;
    private final BotHttpClient botHttpClient;
    private final ChatService chatService;

    private static final String QUESTION_UPDATE_MESSAGE_TEMPLATE = "🔔 Обновления в вопросе на StackOverflow: **%s**%n"
        + "📅 Последняя активность: %s%n" + "🔗 [Смотреть на StackOverflow](%s)";

    private static final String ANSWER_UPDATE_MESSAGE_TEMPLATE = "🔔 Новый ответ на вопрос **%s** на StackOverflow:%n"
        + "👤 Автор: %s%n" + "📅 Время ответа: %s%n" + "🔗 [Смотреть ответ на StackOverflow](%s)";

    public StackOverflowCheckService(
        StackOverflowClient stackOverflowClient,
        LinkService linkService,
        BotHttpClient botHttpClient, ChatService chatService) {
        this.stackOverflowClient = stackOverflowClient;
        this.linkService = linkService;
        this.botHttpClient = botHttpClient;
        this.chatService = chatService;
    }

    @Scheduled(fixedRate = 5000)
    public void checkLinks() {
        log.info("Starting link check...");
        Set<Chat> chatIds = chatService.getAllRegisteredChatIds();
        for (Chat chat : chatIds) {
            List<Link> links = linkService.getAllLinks(chat.chatId());
            for (Link link : links) {
                if (isStackOverflowQuestion(link.url())) {
                    log.info(link.url());
                    checkStackOverflowQuestion(link);
                }
            }
        }
        log.info("Finished link check.");
    }

    private void checkStackOverflowQuestion(Link link) {
        String[] parts = link.url().split("/");
        String questionId = parts[parts.length - 2];

        log.info("id: {}", questionId);

        stackOverflowClient.getQuestion(questionId).subscribe(stackOverflowQuestion -> {
            // Проверяем, если lastUpdated не установлен
            if (link.createdAt() == null) {
                linkService.updateLastUpdated(link.id(), stackOverflowQuestion.getLastActivityDateAsDateTime());
                return;
            }

            // Если есть изменения в вопросе (новая активность)
            if (stackOverflowQuestion.getLastActivityDateAsDateTime() != null
                && !stackOverflowQuestion
                .getLastActivityDateAsDateTime()
                .equals(link.createdAt())) {
                linkService.updateLastUpdated(link.id(), stackOverflowQuestion.getLastActivityDateAsDateTime());
                sendUpdate(link, stackOverflowQuestion);
            }

            // Проверяем новые ответы
            stackOverflowClient.getLatestAnswer(questionId).subscribe(answer -> {
                boolean updated = false;
                List<Long> knownIssueIds = link.knownIssueIds() != null ? link.knownIssueIds() : new ArrayList<>();
                link.knownIssueIds(knownIssueIds);


                if (!knownIssueIds.contains(answer.questionId())) {
                    knownIssueIds.add(answer.questionId());
                    sendAnswerUpdate(link, answer);
                    updated = true;
                }

                if (updated) {
                    linkService.addLink(link.chat().chatId(), link);
                }
            });
        });
    }

    private void sendUpdate(Link link, StackOverflowQuestionAnswer stackOverflowQuestion) {
        // Используем тернарный оператор для проверки null значений
        String title = stackOverflowQuestion.title() != null ? stackOverflowQuestion.title() : "Без названия";
        String lastActivity = stackOverflowQuestion.getLastActivityDateAsDateTime() != null ?
            stackOverflowQuestion.getLastActivityDateAsDateTime().toString() : "Не указано время";

        String message = String.format(
            QUESTION_UPDATE_MESSAGE_TEMPLATE,
            title,
            lastActivity,
            link.url());

        LinkUpdate update = new LinkUpdate(link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));

        log.info("Отправка уведомления: {}", message);
        botHttpClient.sendUpdate(update).subscribe();
    }

    private void sendAnswerUpdate(Link link, StackOverflowQuestionAnswer answer) {
        String title = answer.title() != null ? answer.title() : "Без названия";
        String displayName = answer.owner().name();
        String creationDate = answer.getCreationDate() != null ? answer.getCreationDate().toString() : "Не указано время";

        String message = String.format(
            ANSWER_UPDATE_MESSAGE_TEMPLATE,
            title,
            displayName,
            creationDate,
            link.url());

        LinkUpdate update = new LinkUpdate(link.chat().chatId(), link.url(), message, List.of(link.chat().chatId()));

        log.info("Отправка обновления ответа: {}", message);
        botHttpClient.sendUpdate(update).subscribe();
    }

    boolean isStackOverflowQuestion(String url) {
        log.info("Проверка URL: {}", url);  // Логируем URL перед проверкой
        return url.matches("https://stackoverflow\\.com/questions/\\d+(/.*)?");
    }

}
