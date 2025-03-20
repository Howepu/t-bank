package backend.academy.scrapper.services.jdbc;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.Tag;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.repositories.jdbc.JdbcLinkRepository;
import backend.academy.scrapper.repositories.jdbc.JdbcTagRepository;
import backend.academy.scrapper.services.LinkService;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class JdbcLinkService implements LinkService {

    private final JdbcLinkRepository linkRepository;
    private final ChatService chatService;
    private final JdbcTagRepository tagRepository;

    @Override
    public Link addLink(Long chatId, Link link) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }

        // Получение чата
        Chat chat = chatService.getChatById(chatId);
        link.chat(chat);

        // Обработка тегов
        Set<Tag> persistedTags = new HashSet<>();
        if (link.assignedTags() != null) {
            for (Tag tag : link.assignedTags()) {
                // Проверка, существует ли тег
                Tag existingTag = tagRepository.getByName(tag.name());
                if (existingTag != null) {
                    persistedTags.add(existingTag);
                } else {
                    tagRepository.add(tag.name());
                }
            }
        }

        link.assignedTags(persistedTags);

        return linkRepository.add(link);
    }

    @Override
    public Link removeLink(Long chatId, String url) {
        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }

        if (chatId == null || url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Chat ID и URL должны быть предоставлены и не могут быть пустыми");
        }

        Link link = linkRepository.findByUrl(url);
        linkRepository.delete(url);

        return link;
    }



    @Override
    public List<Link> getAllLinks(Long chatId) {

        return linkRepository.findAllByChatChatId(chatId);
    }

    @Override
    public void updateLastUpdated(Long id, LocalDateTime createdAt) {
        linkRepository.updateCreatedAtById(id, createdAt);
    }
}

