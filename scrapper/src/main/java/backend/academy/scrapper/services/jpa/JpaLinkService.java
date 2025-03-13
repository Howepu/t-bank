package backend.academy.scrapper.services.jpa;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.jpa.JpaLinkRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@AllArgsConstructor
@Service
public class JpaLinkService {


    private final JpaLinkRepository linkRepository;
    private final JpaChatService chatService;

    public Link addLink(Long chatId, Link link) {
        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }

        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        Chat chat = chatService.getChatById(chatId);

        link.chat(chat);

        return linkRepository.save(link);
    }

    public Link removeLink(Long chatId, String url) {
        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }

        if (chatId == null || url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Chat ID и URL должны быть предоставлены и не могут быть пустыми");
        }

        Link link = linkRepository.deleteByUrl(url);
        if (link == null) {
            throw new NoSuchElementException("Ссылка не найдена");
        }

        return link;
    }

    public List<Link> getAllLinks(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        return linkRepository.findAllByChat_ChatId(chatId);
    }

    public void updateLastUpdated(Long id, LocalDateTime createdAt) {
        linkRepository.updateCreatedAtById(id, createdAt);
    }

}
