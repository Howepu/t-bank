package backend.academy.scrapper.repositories;

import backend.academy.scrapper.models.Link;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class InMemoryLinkRepository implements LinkRepository {

    private final List<Link> links = new ArrayList<>();  // Общий список всех ссылок

    @Override
    public Link save(Long chatId, Link link) {
        link.chatId(chatId);
        links.add(link);

        return link;
    }

    @Override
    public Link remove(Long chatId, String url) {
        List<Link> linkList = findAllByChatId(chatId).stream().filter(l -> l.url().equals(url)).toList();

        if (!linkList.isEmpty()) {
            Link link = linkList.getFirst();
            links.remove(link);
            return link;
        }

        return null;
    }

    @Override
    public List<Link> findAllByChatId(Long chatId) {
        List<Link> chatLinks = new ArrayList<>();
        for (Link link : links) {
            if (link.chatId() == chatId) {
                chatLinks.add(link);
            }
        }
        return chatLinks;
    }
}
