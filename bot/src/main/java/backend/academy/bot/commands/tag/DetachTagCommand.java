package backend.academy.bot.commands.tag;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.commands.CommandHandler;
import backend.academy.bot.dto.AttachTagRequest;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class DetachTagCommand implements CommandHandler {

    private final ScrapperHttpClient scrapperHttpClient;
    @Override
    public String command() {
        return "/detach_tag";
    }

    @Override
    public String description() {
        return "Отвязать тег от ссылки";
    }

    @Override
    public SendMessage handle(Update update) {
        long chatId = update.message().chat().id();
        String messageText = update.message().text();

        String[] commandParts = messageText.split(" ", 3);

        if (commandParts.length < 3 || commandParts.length > 4) {
            return new SendMessage(chatId, "Пожалуйста, укажите название тега и ссылки после команды.");
        }

        String url = commandParts[1];
        String tagName = commandParts[2];


        AttachTagRequest request = new AttachTagRequest(url, tagName);

        return scrapperHttpClient.detachTagFromLink(chatId, request)
            .map(response -> new SendMessage(chatId, "Тег \"" + tagName + "\" успешно отвязан от ссылки" +
                ": " + url))
            .onErrorResume(e -> Mono.just(new SendMessage(chatId, "Произошла ошибка при отвязывание.")))
            .block();
    }
}
