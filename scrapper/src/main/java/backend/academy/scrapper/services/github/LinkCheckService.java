package backend.academy.scrapper.services.github;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LinkCheckService extends AbstractLinkCheckService {

    private final BotHttpClient botHttpClient;

    public LinkCheckService(
            GitHubClient gitHubClient,
            LinkService linkService,
            ChatService chatService,
            BotHttpClient botHttpClient,
            MeterRegistry registry) {
        super(gitHubClient, linkService, chatService, registry);
        this.botHttpClient = botHttpClient;
    }

    @Override
    protected void deliverUpdate(LinkUpdate update) {
        botHttpClient.sendUpdate(update).subscribe();
    }
}
