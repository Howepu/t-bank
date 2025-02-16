package backend.academy.scrapper.services;

import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.repositories.InMemoryChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class ChatService {

    private final InMemoryChatRepository chatRepository;

    public void registerChat(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        chatRepository.save(chatId);
    }

    public boolean isChatRegistered(Long chatId) {
        return chatRepository.isChatRegistered(chatId);
    }

    public void removeChat(Long chatId) {

        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        chatRepository.remove(chatId);

        if (!chatRepository.isChatRegistered(chatId)) {
            throw new NoSuchElementException("Chat ID не найден");
        }
    }

}
