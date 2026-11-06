package backend.academy.bot.services;

import backend.academy.bot.clients.TelegramClient;
import backend.academy.bot.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BotService {

    @Value("app.kafka-topic-updates")
    private String topic;

    private final TelegramClient telegramClient;

    public BotService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
    @KafkaListener(topics = "app.kafka-topic-updates", groupId = "group1")
    public void processKafkaUpdate(LinkUpdate update) {
        System.out.println(update.url());
        System.out.println(update.description());
        String message = "Обновления по ссылке: " + update.url() + "\n" + update.description();

        log.info("Получено обновление: {}", message);

        for (Long chatId : update.tgChatIds()) {
            telegramClient.sendMessage(chatId, message);
        }
    }

    public void processUpdate(LinkUpdate update) {
        System.out.println(update.url());
        System.out.println(update.description());
        String message = "Обновления по ссылке: " + update.url() + "\n" + update.description();

        log.info("Получено обновление: {}", message);

        for (Long chatId : update.tgChatIds()) {
            telegramClient.sendMessage(chatId, message);
        }
    }

    @DltHandler
    public void listenDLT(LinkUpdate update) {
        log.info("DLT Received: URL: {}, Topic: {}", update.url(), topic);
    }
}
