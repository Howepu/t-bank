package backend.academy.scrapper.repositories;

import backend.academy.scrapper.models.Chat;

public interface ChatRepository {
    void save(Long id);
    void remove(Long id);
    boolean isChatRegistered(Long id);
}
