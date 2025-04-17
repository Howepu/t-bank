package backend.academy.scrapper.services.stackoverflow;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.stackoverflow.StackOverflowClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StackOverflowCheckService extends AbstractStackoverflowCheckService {

    private final BotHttpClient botHttpClient;

    public StackOverflowCheckService(
            StackOverflowClient stackOverflowClient,
            LinkService linkService,
            BotHttpClient botHttpClient,
            ChatService chatService) {
        super(stackOverflowClient, linkService, chatService);
        this.botHttpClient = botHttpClient;
    }

    @Override
    protected void deliverUpdate(LinkUpdate update) {
        botHttpClient.sendUpdate(update).subscribe();
    }
}
