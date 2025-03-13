//package backend.academy.scrapper.services;
//
//import backend.academy.scrapper.repositories.InMemoryChatRepository;
//import java.util.NoSuchElementException;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@AllArgsConstructor
//public class ChatService {
//
//    private final InMemoryChatRepository chatRepository;
//
//    public void registerChat(Long chatId) {
//        if (chatId == null) {
//            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
//        }
//
//        chatRepository.save(chatId);
//    }
//
//    public boolean isChatRegistered(Long chatId) {
//        return chatRepository.isChatRegistered(chatId);
//    }
//
//    public void removeChat(Long chatId) {
//
//        if (chatId == null) {
//            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
//        }
//
//        chatRepository.remove(chatId);
//
//        if (!chatRepository.isChatRegistered(chatId)) {
//            throw new NoSuchElementException("Chat ID не найден");
//        }
//    }
//}
