package backend.academy.scrapper.repositories;

import backend.academy.scrapper.models.Link;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class InMemoryLinkRepository implements LinkRepository {

    private List<Link> links = new ArrayList<>();

    @Override
    public void save(Link link) {
        links.add(link);
    }

    @Override
    public void remove(String url) {
        links.removeIf(link -> link.url().equals(url));
    }

    @Override
    public List<Link> findAll() {
        return links;
    }

    @Override
    public Link findByUrl(String url) {
        return links.stream()
            .filter(link -> link.url().equals(url))
            .findFirst()
            .orElse(null);
    }
}
