package backend.academy.bot.commands.tag;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.commands.CommandHandler;
import backend.academy.bot.commands.UpdateUtils;
import backend.academy.bot.dto.AttachTagRequest;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public abstract class BaseTagCommand implements CommandHandler {
    protected final ScrapperHttpClient scrapperHttpClient;

    protected abstract String getSuccessMessage(String tagName, String url);

    protected abstract String getErrorMessage();

    protected abstract Mono<?> executeScrapperAction(long chatId, AttachTagRequest request);

    @Override
    public SendMessage handle(Update update) {
        long chatId = UpdateUtils.getChatId(update);
        String messageText = UpdateUtils.getMessageText(update);

        String[] commandParts = messageText.split(" ", 3);

        if (commandParts.length < 3 || commandParts.length > 4) {
            return new SendMessage(chatId, "Пожалуйста, укажите название тега и ссылки после команды.");
        }

        String url = commandParts[1];
        String tagName = commandParts[2];

        AttachTagRequest request = new AttachTagRequest(url, tagName);

        return executeScrapperAction(chatId, request)
                .map(response -> new SendMessage(chatId, getSuccessMessage(tagName, url)))
                .onErrorResume(e -> Mono.just(new SendMessage(chatId, getErrorMessage())))
                .block();
    }
}
