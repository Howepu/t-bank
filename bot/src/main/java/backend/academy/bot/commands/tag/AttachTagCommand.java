package backend.academy.bot.commands.tag;

import backend.academy.bot.clients.ScrapperHttpClient;
import backend.academy.bot.dto.AttachTagRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AttachTagCommand extends BaseTagCommand {

    public AttachTagCommand(ScrapperHttpClient scrapperHttpClient) {
        super(scrapperHttpClient);
    }

    @Override
    public String command() {
        return "/attach_tag";
    }

    @Override
    public String description() {
        return "Привязать тег к ссылке";
    }

    @Override
    protected String getSuccessMessage(String tagName, String url) {
        return "Тег \"" + tagName + "\" успешно привязан к ссылке" + ": " + url;
    }

    @Override
    protected String getErrorMessage() {
        return "Произошла ошибка при связывании.";
    }

    @Override
    protected Mono<?> executeScrapperAction(long chatId, AttachTagRequest request) {
        return scrapperHttpClient.attachTagToLink(chatId, request);
    }
}
