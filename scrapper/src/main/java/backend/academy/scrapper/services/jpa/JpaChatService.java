package backend.academy.scrapper.services.jpa;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.repositories.jpa.JpaChatRepository;
import backend.academy.scrapper.repositories.jpa.JpaLinkRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.NoSuchElementException;
import java.util.Set;

@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jpa")
public class JpaChatService implements ChatService {


    private final JpaChatRepository chatRepository;
    private final JpaLinkRepository linkRepository;


    @Override
    public void registerChat(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        chatRepository.saveByChatId(chatId);
    }

    @Override
    public boolean isChatRegistered(Long chatId) {
        return chatRepository.existsByChatId(chatId);
    }

    @Override
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

    @Override
    public Set<Chat> getAllRegisteredChatIds() {
        return chatRepository.getAllChats();
    }

    @Override
    public Chat getChatById(Long chatId) {
        return chatRepository.getChatByChatId(chatId);
    }
}
