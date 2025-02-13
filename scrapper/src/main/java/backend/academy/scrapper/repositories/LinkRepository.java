package backend.academy.scrapper.repositories;

import backend.academy.scrapper.models.Link;
import java.util.List;

public interface LinkRepository {
    Link save(Long chatId, Link link);
    Link remove(Long chatId, String url);
    List<Link> findAllByChatId(Long chatId);
}
