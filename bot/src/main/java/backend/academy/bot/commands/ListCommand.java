package backend.academy.bot.commands;

import backend.academy.bot.ScrapperHttpClient;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class ListCommand implements CommandHandler {

    private final ScrapperHttpClient scrapperHttpClient;

    public ListCommand(ScrapperHttpClient scrapperHttpClient) {
        this.scrapperHttpClient = scrapperHttpClient;
    }

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

        return scrapperHttpClient.getLinks(chatId)
            .map(response -> {
                if (response.links().isEmpty()) {
                    return new SendMessage(chatId, "Вы пока не отслеживаете ни одной ссылки.");
                }
                String links = response.links().stream()
                    .map(link -> "- " + link.url())
                    .reduce("", (a, b) -> a + "\n" + b);
                return new SendMessage(chatId, "Ваши отслеживаемые ссылки:\n" + links);
            })
            .defaultIfEmpty(new SendMessage(chatId, "Ошибка получения списка ссылок."))
            .block();
    }
}
