package backend.academy.bot.commands;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.dto.RemoveLinkRequest;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UntrackCommand implements CommandHandler {

    private final ScrapperHttpClient scrapperHttpClient;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String CACHE_PREFIX = "bot:list:";

    @Override
    public String command() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "Удалить отслеживаемую ссылку";
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String messageText = update.message().text().trim();

        if (messageText.equals("/untrack")) {
            return new SendMessage(chatId, "Введите ссылку, которую хотите удалить.")
                    .replyToMessageId(update.message().messageId());
        }

        String[] parts = messageText.split("\\s+", 2);
        if (parts.length < 2 || parts[1].trim().isEmpty()) {
            return new SendMessage(chatId, "Пожалуйста, укажите ссылку после команды /untrack")
                    .replyToMessageId(update.message().messageId());
        }

        String urlToRemove = parts[1].trim();
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(urlToRemove);

        try {
            scrapperHttpClient.removeLink(chatId, removeLinkRequest).block();

            redisTemplate.delete(CACHE_PREFIX + chatId);

            return new SendMessage(chatId, "Ссылка удалена: " + removeLinkRequest.url());
        } catch (Exception e) {
            return new SendMessage(chatId, "Ошибка! Ссылка не найдена или не отслеживается: " + removeLinkRequest.url())
                    .replyToMessageId(update.message().messageId());
        }
    }
}
