package backend.academy.bot.commands.tag;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.commands.CommandHandler;
import backend.academy.bot.dto.AttachTagRequest;
import backend.academy.bot.dto.TagRequest;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class AttachTagCommand implements CommandHandler {

    private final ScrapperHttpClient scrapperHttpClient;

    @Override
    public String command() {
        return "/attach_tag";
    }

    @Override
    public String description() {
        return "Привязать тег к ссылке";
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

        return scrapperHttpClient.attachTagToLink(chatId, request)
            .map(response -> new SendMessage(chatId, "Тег \"" + tagName + "\" успешно привязан к ссылке" +
                ": " + url))
            .onErrorResume(e -> Mono.just(new SendMessage(chatId, "Произошла ошибка при связывание.")))
            .block();
    }
}
