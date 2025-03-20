package backend.academy.scrapper.access;

import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.repositories.jdbc.JdbcChatRepository;
import backend.academy.scrapper.repositories.jdbc.JdbcLinkRepository;
import backend.academy.scrapper.repositories.jdbc.JdbcTagRepository;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.services.jdbc.JdbcChatService;
import backend.academy.scrapper.services.jdbc.JdbcLinkService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc")
public class JdbcAccessConfiguration {

    @Bean
    public ChatService chatService(
        JdbcChatRepository chatRepository
    ) {
        return new JdbcChatService(chatRepository);
    }

    @Bean
    public LinkService linkService(
        JdbcLinkRepository jpaLinkRepository,
        JdbcChatService chatService,
        JdbcTagRepository tagRepository
    ) {
        return new JdbcLinkService(jpaLinkRepository, chatService, tagRepository);
    }
}
