package backend.academy.bot.commands.tag;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.commands.CommandHandler;
import backend.academy.bot.dto.TagResponse;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
@AllArgsConstructor
public class TagsCommand implements CommandHandler {

    private final ScrapperHttpClient scrapperHttpClient;
    @Override
    public String command() {
        return "/tags";
    }

    @Override
    public String description() {
        return "Показать все теги пользователя";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        return scrapperHttpClient.getTags(chatId)
            .map(tagsResponse -> {
                // Получаем список объектов TagResponse
                List<TagResponse> tags = tagsResponse.tags();
                StringBuilder response = new StringBuilder("Ваши теги:\n");

                // Перебираем теги и выводим только имя каждого тега
                tags.forEach(tag -> response.append(tag.name()).append("\n"));

                return new SendMessage(chatId, response.toString());
            })
            .onErrorResume(e -> Mono.just(new SendMessage(chatId, "Произошла ошибка при получении тегов.")))
            .block();  // Блокируем для синхронного выполнения
    }
}
