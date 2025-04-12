package backend.academy.scrapper.access;

import backend.academy.scrapper.bot.BotHttpClient;
import backend.academy.scrapper.github.GitHubClient;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.services.github.AbstractLinkCheckService;
import backend.academy.scrapper.services.github.LinkCheckService;
import backend.academy.scrapper.services.stackoverflow.AbstractStackoverflowCheckService;
import backend.academy.scrapper.services.stackoverflow.StackOverflowCheckService;
import backend.academy.scrapper.stackoverflow.StackOverflowClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "http")
public class HttpAccessConfiguration {

    @Bean
    public AbstractLinkCheckService linkCheckService(
            GitHubClient gitHubClient, LinkService linkService, ChatService chatService, BotHttpClient botHttpClient) {
        return new LinkCheckService(gitHubClient, linkService, chatService, botHttpClient);
    }

    @Bean
    public AbstractStackoverflowCheckService stackoverflowCheckService(
        StackOverflowClient stackOverflowClient,
        LinkService linkService,
        BotHttpClient botHttpClient,
        ChatService chatService) {
        return new StackOverflowCheckService(stackOverflowClient, linkService, botHttpClient, chatService);
    }
}
