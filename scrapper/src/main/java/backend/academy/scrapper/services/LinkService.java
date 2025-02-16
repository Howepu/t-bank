package backend.academy.scrapper.services;

import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.repositories.LinkRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final ChatService chatService;

    public Link addLink(Long chatId, Link link) {
        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }

        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        return linkRepository.save(chatId, link);
    }

    public Link removeLink(Long chatId, String url) {
        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }

        if (chatId == null || url == null || url.isEmpty()) {
            throw new IllegalArgumentException("Chat ID и URL должны быть предоставлены и не могут быть пустыми");
        }

        Link link = linkRepository.remove(chatId, url);
        if (link == null) {
            throw new NoSuchElementException("Ссылка не найдена");
        }

        return link;
    }

    public List<Link> getAllLinks(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        return linkRepository.findAllByChatId(chatId);
    }
}
