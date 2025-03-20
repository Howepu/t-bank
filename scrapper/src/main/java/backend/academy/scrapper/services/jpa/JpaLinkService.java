package backend.academy.scrapper.services.jpa;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.models.Tag;
import backend.academy.scrapper.repositories.jpa.JpaLinkRepository;
import backend.academy.scrapper.repositories.jpa.JpaTagRepository;
import backend.academy.scrapper.services.LinkService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class JpaLinkService implements LinkService {


    private final JpaLinkRepository linkRepository;
    private final JpaChatService chatService;
    private final JpaTagRepository tagRepository;

    @Transactional
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
                Tag existingTag = tagRepository.findByName(tag.name())
                    .orElseGet(() -> tagRepository.save(tag)); // Сохраняем новый, если не найден
                persistedTags.add(existingTag);
            }
        }

        link.assignedTags(persistedTags);

        return linkRepository.save(link);
    }

    @Override
    public Link removeLink(Long chatId, String url) {
        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }

        if (chatId == null || url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Chat ID и URL должны быть предоставлены и не могут быть пустыми");
        }

        Set<Tag> tags = linkRepository.findByUrl(url).assignedTags();

        Link link = linkRepository.findAndDeleteByUrl(url);

        for (Tag tag : tags) {
            tagRepository.delete(tag);
        }

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
