package backend.academy.scrapper.services.jpa;

import backend.academy.scrapper.models.Tag;
import backend.academy.scrapper.repositories.jpa.JpaTagRepository;
import backend.academy.scrapper.services.TagService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class JpaTagService implements TagService {

    private final JpaTagRepository tagRepository;

    @Override
    public List<Tag> findAll(Long chatId) {
        return tagRepository.findAllByChatId(chatId);
    }

    @Override
    public void saveByName(String name, Long chatId) {
        if (tagRepository.findTagByChatIdAndName(chatId, name) == null) {

            tagRepository.saveTag(name, chatId);

        } else {
            log.info("Такой тег {} в чате {} уже существует", name, chatId);
        }
    }
}
