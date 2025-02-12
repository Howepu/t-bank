package backend.academy.scrapper.services;

import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.repositories.LinkRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;

    public void addLink(Link link) {
        linkRepository.save(link);
    }

    public void removeLink(String url) {
        linkRepository.remove(url);
    }

    public List<Link> getAllLinks() {
        return linkRepository.findAll();
    }

    public Link getLinkByUrl(String url) {
        return linkRepository.findByUrl(url);
    }

}
