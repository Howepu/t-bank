//package backend.academy.scrapper.repositories;
//
//import backend.academy.scrapper.models.Chat;
//import java.util.HashSet;
//import java.util.Objects;
//import java.util.Set;
//import org.springframework.stereotype.Repository;
//
//@Repository
//public class InMemoryChatRepository implements ChatRepository {
//
//    private static final Set<Chat> registeredChats = new HashSet<>();
//
//    @Override
//    public void save(Long id) {
//        registeredChats.add(new Chat(id));
//    }
//
//    @Override
//    public void remove(Long id) {
//        registeredChats.removeIf(chat -> Objects.equals(chat.id(), id));
//    }
//
//    @Override
//    public boolean isChatRegistered(Long id) {
//        for (Chat chat : registeredChats) {
//            if (Objects.equals(chat.id(), id)) {
//                return true;
//            }
//        }
//        return false;
//    }
//}
