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
    public List<Tag> findAll (Long chatId) {
        return tagRepository.findAllByChatId(chatId);
    }

    @Override
    public void saveByName(String name, Long chatId) {
        if(tagRepository.findTagByChatIdAndName(chatId, name) == null) {

            tagRepository.saveTag(name, chatId);

        }
    }
}
