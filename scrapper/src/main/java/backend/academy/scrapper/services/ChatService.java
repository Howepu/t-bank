package backend.academy.scrapper.services;

import backend.academy.scrapper.repositories.InMemoryChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ChatService {

    private final InMemoryChatRepository chatRepository;

    public void registerChat(Long chatId) {
        chatRepository.save(chatId);
    }

    public boolean isChatRegistered(Long chatId) {
        return chatRepository.isChatRegistered(chatId);
    }

    public void removeChat(Long chatId) {
        chatRepository.remove(chatId);
    }

}
