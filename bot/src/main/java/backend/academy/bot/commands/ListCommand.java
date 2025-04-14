package backend.academy.bot.commands;

import backend.academy.bot.clients.ScrapperHttpClient;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@AllArgsConstructor
public class ListCommand implements CommandHandler {

    private final ScrapperHttpClient scrapperHttpClient;
    private final RedisTemplate<String, String> redisTemplate; // Изменено на String

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);
    private static final String CACHE_PREFIX = "bot:list:";

    @Override
    public String command() {
        return "/list";
    }

    @Override
    public String description() {
        return "Показать список отслеживаемых ссылок";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        String cacheKey = CACHE_PREFIX + chatId;

        // Попытка получить данные из кэша
        String cachedResponse = redisTemplate.opsForValue().get(cacheKey);
        if (cachedResponse != null) {
            log.info("Кэш");
            return new SendMessage(chatId, cachedResponse);
        }

        // Если в кэше нет данных, выполняем запрос к API
        SendMessage response = scrapperHttpClient
            .getLinks(chatId)
            .map(apiResponse -> {
                if (apiResponse.links().isEmpty()) {
                    return new SendMessage(chatId, "Вы пока не отслеживаете ни одной ссылки.");
                }
                String links = apiResponse.links().stream()
                    .map(link -> "- " + link.url())
                    .reduce("", (a, b) -> a + "\n" + b);
                return new SendMessage(chatId, "Ваши отслеживаемые ссылки:\n" + links);
            })
            .defaultIfEmpty(new SendMessage(chatId, "Ошибка получения списка ссылок."))
            .block();

        // Сохраняем только текст сообщения в кэш
        if (response != null) {
            redisTemplate.opsForValue().set(cacheKey, response.getParameters().get("text").toString(), CACHE_TTL);
        }

        return response;
    }
}
