package backend.academy.bot.clients;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TelegramClient {
    private final TelegramBot telegramBot;

    @Value("${app.telegram-token}")
    private String botToken;

    public TelegramClient() {
        this.telegramBot = new TelegramBot(botToken);
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage request = new SendMessage(chatId, message);
        log.info("Отправка сообщения в chatId: {} - {}", chatId, message);
        telegramBot.execute(request);
    }
}
