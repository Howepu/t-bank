package backend.academy.scrapper.repositories;

import backend.academy.scrapper.models.Link;
import java.util.List;


public interface LinkRepository {
    void save(Link link);
    void remove(String url);
    List<Link> findAll();
    Link findByUrl(String url);
}
