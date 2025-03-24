package backend.academy.scrapper.services.jdbc;

import backend.academy.scrapper.models.Tag;
import backend.academy.scrapper.repositories.jdbc.JdbcTagRepository;
import backend.academy.scrapper.services.TagService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
@Slf4j
public class JdbcTagService implements TagService {

    private final JdbcTagRepository tagRepository;
    @Override
    public List<Tag> findAll (Long chatId) {
        return tagRepository.getAllByChatId(chatId);
    }

    @Override
    public void saveByName(String name, Long chatId) {
        log.debug("Попытка сохранить тег: name={}, chatId={}", name, chatId);
        Optional<Tag> existingTag = tagRepository.getByNameAndChatId(name, chatId);

        if (existingTag.isEmpty()) {
            log.info("Добавление тега: {}", name);
            tagRepository.add(name, chatId);
        } else {
            log.info("Тег уже есть: {}", name);
        }
    }
}
