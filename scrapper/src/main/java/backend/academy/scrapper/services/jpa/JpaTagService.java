package backend.academy.scrapper.services.jpa;

import backend.academy.scrapper.models.Tag;
import backend.academy.scrapper.repositories.jpa.JpaLinkRepository;
import backend.academy.scrapper.repositories.jpa.JpaTagRepository;
import backend.academy.scrapper.services.TagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@AllArgsConstructor
@Service
public class JpaTagService implements TagService {

    private final JpaTagRepository tagRepository;
    private final JpaLinkRepository linkRepository;


    @Override
    public Tag getTagByName(String name, Long chatId) {
        return null;
    }

    @Override
    public void saveByName(String name) {
        if(tagRepository.findByName(name) == null) {

            tagRepository.saveTag(name);

        }
    }
}
