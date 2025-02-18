package backend.academy.scrapper.services;

import backend.academy.scrapper.stackoverflow.StackOverflowClient;
import backend.academy.scrapper.stackoverflow.StackOverflowQuestion;
import backend.academy.scrapper.repositories.InMemoryLinkRepository;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class StackOverflowCheckService {

    private final StackOverflowClient stackOverflowClient;
    private final InMemoryLinkRepository linkRepository;
    private final BotHttpClient botHttpClient;

    public StackOverflowCheckService(StackOverflowClient stackOverflowClient, InMemoryLinkRepository linkRepository,
                                     BotHttpClient botHttpClient) {
        this.stackOverflowClient = stackOverflowClient;
        this.linkRepository = linkRepository;
        this.botHttpClient = botHttpClient;
    }

    @Scheduled(fixedRate = 5000)
    public void checkLinks() {
        for (Link link : linkRepository.findAll()) {
            if (isStackOverflowQuestion(link.url())) {
                checkStackOverflowQuestion(link);
            }
        }
    }

    private void checkStackOverflowQuestion(Link link) {
        String[] parts = link.url().split("/");
        String questionId = parts[parts.length - 1];

        stackOverflowClient.getQuestion(questionId)
            .subscribe(stackOverflowQuestion -> {

                if (!linkRepository.isInitialized(link.url())) {
                    if (linkRepository.getLastUpdated(link.url()) == null) {
                        linkRepository.updateLastUpdated(link.url(), stackOverflowQuestion.getLastActivityDateAsDateTime());
                    }
                    linkRepository.markInitialized(link.url());
                    return;
                }

                if (stackOverflowQuestion.lastActivityDate() != null && !stackOverflowQuestion.getLastActivityDateAsDateTime()
                    .equals(linkRepository.getLastUpdated(link.url()))) {
                    linkRepository.updateLastUpdated(link.url(), stackOverflowQuestion.getLastActivityDateAsDateTime());
                    sendUpdate(link, stackOverflowQuestion);
                }
            });
    }

    private void sendUpdate(Link link, StackOverflowQuestion stackOverflowQuestion) {
        String message = String.format(
            "🔔 Обновления в вопросе на StackOverflow: **%s**\n" +
                "📅 Последняя активность: %s\n" +
                "🔗 [Смотреть на StackOverflow](%s)",
            stackOverflowQuestion.title(),
            stackOverflowQuestion.getLastActivityDateAsDateTime(),
            link.url()
        );

        LinkUpdate update = new LinkUpdate(
            link.chatId(),
            link.url(),
            message,
            List.of(link.chatId())
        );

        log.info("Отправка уведомления: {}", message);
        botHttpClient.sendUpdate(update).subscribe();
    }

    boolean isStackOverflowQuestion(String url) {
        // Проверяем, что URL соответствует формату вопроса на StackOverflow
        return url.matches("https://stackoverflow\\.com/questions/\\d+");
    }
}
