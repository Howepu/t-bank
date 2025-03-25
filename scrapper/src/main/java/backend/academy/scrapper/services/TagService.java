package backend.academy.scrapper.services;

import backend.academy.scrapper.models.Tag;
import java.util.List;

public interface TagService {
    List<Tag> findAll(Long chatId);

    void saveByName(String name, Long chatId);
}
