package backend.academy.scrapper.services;

import backend.academy.scrapper.models.Link;

import java.time.LocalDateTime;
import java.util.List;

public interface LinkService {

    Link addLink(Long chatId, Link link);
    Link removeLink(Long chatId, String url);
    List<Link> getAllLinks(Long chatId);
    void updateLastUpdated(Long id, LocalDateTime createdAt);
}
