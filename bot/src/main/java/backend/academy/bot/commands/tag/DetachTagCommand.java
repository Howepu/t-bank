package backend.academy.bot.commands.tag;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.dto.AttachTagRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DetachTagCommand extends BaseTagCommand {

    public DetachTagCommand(ScrapperHttpClient scrapperHttpClient) {
        super(scrapperHttpClient);
    }

    @Override
    public String command() {
        return "/detach_tag";
    }

    @Override
    public String description() {
        return "Отвязать тег от ссылки";
    }

    @Override
    protected String getSuccessMessage(String tagName, String url) {
        return "Тег \"" + tagName + "\" успешно отвязан от ссылки" + ": " + url;
    }

    @Override
    protected String getErrorMessage() {
        return "Произошла ошибка при отвязывании.";
    }

    @Override
    protected Mono<?> executeScrapperAction(long chatId, AttachTagRequest request) {
        return scrapperHttpClient.detachTagFromLink(chatId, request);
    }
}
