package backend.academy.scrapper.services;

import backend.academy.scrapper.models.Tag;

public interface TagService {
    Tag getTagByName(String name, Long chatId);
    void saveByName(String name);
}
