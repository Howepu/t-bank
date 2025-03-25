package backend.academy.bot.commands.tag;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.commands.CommandHandler;
import backend.academy.bot.dto.TagRequest;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class AddTagCommand implements CommandHandler {

    private final ScrapperHttpClient scrapperHttpClient;

    @Override
    public String command() {
        return "/addtag";
    }

    @Override
    public String description() {
        return "Добавить новый тег";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        String messageText = update.message().text();

        String[] commandParts = messageText.split(" ", 2);

        if (commandParts.length < 2) {
            return new SendMessage(chatId, "Пожалуйста, укажите название тега после команды.");
        }

        String tagName = commandParts[1]; // Получаем название тега

        // Создаем запрос для добавления тега
        TagRequest request = new TagRequest(tagName);

        // Добавляем тег через сервис
        return scrapperHttpClient
                .addTag(chatId, request)
                .map(response -> new SendMessage(chatId, "Тег \"" + tagName + "\" успешно добавлен."))
                .onErrorResume(e -> Mono.just(new SendMessage(chatId, "Произошла ошибка при добавлении тега.")))
                .block(); // блокируем для синхронного выполнения
    }
}
