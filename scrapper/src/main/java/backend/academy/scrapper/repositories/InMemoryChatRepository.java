package backend.academy.scrapper.repositories;

import backend.academy.scrapper.models.Chat;
import org.springframework.stereotype.Repository;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Repository
public class InMemoryChatRepository implements ChatRepository {

    private final Set<Chat> registeredChats = new HashSet<>();


    @Override
    public void save(Long id) {
        registeredChats.add(new Chat(id));
    }

    @Override
    public void remove(Long id) {
        for (Chat chat : registeredChats) {
            if (Objects.equals(chat.id(), id)) {
                registeredChats.remove(chat);
            }
        }
    }

    @Override
    public boolean isChatRegistered(Long id) {
        for (Chat chat : registeredChats) {
            if (Objects.equals(chat.id(), id)) {
                return true;
            }
        }
        return false;
    }
}
