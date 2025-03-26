package backend.academy.bot.services;

import backend.academy.bot.clients.TelegramClient;
import backend.academy.bot.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BotService {

    private final TelegramClient telegramClient;

    public BotService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
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
}
