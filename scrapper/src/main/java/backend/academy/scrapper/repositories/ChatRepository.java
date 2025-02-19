package backend.academy.scrapper.repositories;

public interface ChatRepository {
    void save(Long id);

    void remove(Long id);

    boolean isChatRegistered(Long id);
}
