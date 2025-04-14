package backend.academy.scrapper.services.stackoverflow;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.stackoverflow.StackOverflowClient;
import backend.academy.scrapper.stackoverflow.StackOverflowQuestionAnswer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public abstract class AbstractStackoverflowCheckService {

    private final StackOverflowClient stackOverflowClient;
    private final LinkService linkService;
    private final ChatService chatService;

    private static final int BATCH_SIZE = 250;
    private static final int NUM_THREADS = 4;

    private final ExecutorService executorService;

    public AbstractStackoverflowCheckService(
            StackOverflowClient stackOverflowClient,
            LinkService linkService,
            ChatService chatService) {
        this.stackOverflowClient = stackOverflowClient;
        this.linkService = linkService;
        this.chatService = chatService;
        this.executorService = Executors.newFixedThreadPool(NUM_THREADS);
    }

    @Scheduled(fixedRate = 5000)
    public void checkLinks() {
        log.info("Starting link check...");
        Set<Chat> chatIds = chatService.getAllRegisteredChatIds();
        for (Chat chat : chatIds) {
            List<Link> links = linkService.getAllLinks(chat.chatId());

            // Разделяем ссылки на батчи по 250
            int totalLinks = links.size();
            for (int i = 0; i < totalLinks; i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, totalLinks);
                List<Link> batch = links.subList(i, end);

                // Обрабатываем каждый батч асинхронно
                processLinksBatch(batch);
            }
        }
        log.info("Finished link check.");
    }

    public void processLinksBatch(@NotNull List<Link> links) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Link link : links) {
            if (isStackOverflowQuestion(link.url())) {
                log.info("Processing link: {}", link.url());
                // Используем пул потоков для асинхронной обработки
                futures.add(CompletableFuture.runAsync(() -> checkStackOverflowQuestion(link), executorService));
            }
        }

        // Ожидаем завершения всех задач в батче
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void checkStackOverflowQuestion(Link link) {
        String[] parts = link.url().split("/");
        String questionId = parts[parts.length - 2];

        log.info("id: {}", questionId);

        stackOverflowClient.getQuestion(questionId).subscribe(stackOverflowQuestion -> {
            // Логика обработки вопросов
            if (link.createdAt() == null) {
                linkService.updateLastUpdated(link.id(), stackOverflowQuestion.getLastActivityDateAsDateTime());
                return;
            }

            // Обработка изменений
            if (stackOverflowQuestion.getLastActivityDateAsDateTime() != null
                    && !stackOverflowQuestion.getLastActivityDateAsDateTime().equals(link.createdAt())) {
                linkService.updateLastUpdated(link.id(), stackOverflowQuestion.getLastActivityDateAsDateTime());
                sendUpdate(link, stackOverflowQuestion);
            }

            // Проверка новых ответов
            stackOverflowClient.getLatestAnswer(questionId).subscribe(answer -> {
                boolean updated = false;
                List<Long> knownIssueIds = link.knownIssueIds() != null ? link.knownIssueIds() : new ArrayList<>();
                link.knownIssueIds(knownIssueIds);

                for (String filter : linkService.getFilterNamesByLink(link.id())) {
                    if (isUserFilter(filter)) {
                        String[] filterParts = filter.split("=");
                        String userName = filterParts[1];
                        if (Objects.equals(userName, answer.owner().name())) {
                            log.info("Пропускаем обновление ответа от пользователя {}", userName);
                            return;
                        }
                    }
                }

                if (!knownIssueIds.contains(answer.questionId())) {
                    knownIssueIds.add(answer.questionId());
                    sendAnswerUpdate(link, answer);
                    updated = true;
                }

                if (updated) {
                    linkService.updateLinkIssues(link.id(), knownIssueIds);
                }
            });
        });
    }

    boolean isStackOverflowQuestion(String url) {
        log.info("Проверка URL: {}", url); // Логируем URL перед проверкой
        return url.matches("https://stackoverflow\\.com/questions/\\d+(/.*)?");
    }

    private boolean isUserFilter(String str) {
        return str != null && str.matches("^user=[a-z0-9_.]+$");
    }

    protected abstract void sendUpdate(Link link, StackOverflowQuestionAnswer stackOverflowQuestion);

    protected abstract void sendAnswerUpdate(Link link, StackOverflowQuestionAnswer answer);
}
