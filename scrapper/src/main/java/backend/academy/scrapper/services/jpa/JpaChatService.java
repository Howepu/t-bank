package backend.academy.scrapper.services.jpa;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.repositories.jpa.JpaChatRepository;
import backend.academy.scrapper.repositories.jpa.JpaLinkRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class JpaChatService {


    private final JpaChatRepository chatRepository;
    private final JpaLinkRepository linkRepository;

    public void registerChat(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        chatRepository.saveByChatId(chatId);
    }

    public boolean isChatRegistered(Long chatId) {
        return chatRepository.existsByChatId(chatId);
    }

    public void removeChat(Long chatId) {

        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        if (!isChatRegistered(chatId)) {
            throw new NoSuchElementException("Chat ID не найден");
        }

        chatRepository.deleteByChatId(chatId);
        Chat chat = chatRepository.findById(chatId).get();
        linkRepository.findAndDeleteByChat(chat);



    }

    public Chat getChatById(Long chatId) {
        return chatRepository.getChatByChatId(chatId);
    }
}
