package backend.academy.bot.commands.tag;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.commands.CommandHandler;
import backend.academy.bot.dto.TagRequest;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class TagCommand implements CommandHandler {

    private final ScrapperHttpClient scrapperHttpClient;

    @Override
    public String command() {
        return "/tag";
    }

    @Override
    public String description() {
        return "Показать ссылки привязанные к тегу";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        String messageText = update.message().text();

        String[] commandParts = messageText.split(" ", 2);

        if (commandParts.length < 2) {
            return new SendMessage(chatId, "Пожалуйста, укажите название тега после команды.");
        }

        String tagName = commandParts[1];

        TagRequest request = new TagRequest(tagName);

        return scrapperHttpClient
                .getLinksByTag(chatId, request)
                .map(response -> {
                    if (response.links().isEmpty()) {
                        return new SendMessage(chatId, "Вы пока не отслеживаете ни одной ссылки.");
                    }
                    String links = response.links().stream()
                            .map(link -> "- " + link.url())
                            .reduce("", (a, b) -> a + "\n" + b);
                    return new SendMessage(chatId, "Ваши отслеживаемые ссылки по тегу " + tagName + ":\n" + links);
                })
                .defaultIfEmpty(new SendMessage(chatId, "Ошибка получения списка ссылок."))
                .block();
    }
}
