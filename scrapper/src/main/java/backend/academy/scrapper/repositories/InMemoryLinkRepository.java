package backend.academy.scrapper.repositories;

import backend.academy.scrapper.models.Link;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Repository
public class InMemoryLinkRepository implements LinkRepository {

    private final List<Link> links = new ArrayList<>();
    private final Map<String, LocalDateTime> linkLastUpdated = new HashMap<>();
    private final Set<String> initializedLinks = new HashSet<>();


    @Override
    public Link save(Long chatId, Link link) {
        link.chatId(chatId);
        links.add(link);
        linkLastUpdated.put(link.url(), LocalDateTime.now());
        return link;
    }

    @Override
    public Link remove(Long chatId, String url) {
        List<Link> linkList = findAllByChatId(chatId).stream().filter(l -> l.url().equals(url)).toList();

        if (!linkList.isEmpty()) {
            Link link = linkList.getFirst();
            links.remove(link);
            linkLastUpdated.remove(url);
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

    @Override
    public List<Link> findAll() {
        return new ArrayList<>(links);
    }

    public LocalDateTime getLastUpdated(String url) {
        return linkLastUpdated.get(url);
    }

    public void updateLastUpdated(String url, LocalDateTime pushedAt) {
        linkLastUpdated.put(url, pushedAt);
    }

    public boolean isInitialized(String url) {
        return initializedLinks.contains(url);
    }

    public void markInitialized(String url) {
        initializedLinks.add(url);
    }

}
