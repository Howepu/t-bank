package backend.academy.scrapper.services;

import backend.academy.scrapper.models.Chat;
import java.util.Set;

public interface ChatService {
    void registerChat(Long chatId);

    boolean isChatRegistered(Long chatId);

    Chat getChatById(Long chatId);

    void removeChat(Long chatId);

    Set<Chat> getAllRegisteredChatIds();
}
