package backend.academy.scrapper.access;

import backend.academy.scrapper.repositories.jpa.JpaChatRepository;
import backend.academy.scrapper.repositories.jpa.JpaLinkRepository;
import backend.academy.scrapper.repositories.jpa.JpaTagRepository;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.services.LinkService;
import backend.academy.scrapper.services.TagService;
import backend.academy.scrapper.services.jpa.JpaChatService;
import backend.academy.scrapper.services.jpa.JpaLinkService;
import backend.academy.scrapper.services.jpa.JpaTagService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JpaAccessConfiguration {

    @Bean
    public ChatService chatService(JpaChatRepository chatRepository, JpaLinkRepository linkRepository) {
        return new JpaChatService(chatRepository, linkRepository);
    }

    @Bean
    public LinkService linkService(
            JpaLinkRepository jpaLinkRepository, JpaChatService chatService, JpaTagRepository tagRepository) {
        return new JpaLinkService(jpaLinkRepository, chatService, tagRepository);
    }

    @Bean
    public TagService tagService(JpaTagRepository tagRepository) {
        return new JpaTagService(tagRepository);
    }
}
