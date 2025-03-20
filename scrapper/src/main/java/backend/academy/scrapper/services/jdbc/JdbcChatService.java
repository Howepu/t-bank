package backend.academy.scrapper.services.jdbc;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.services.ChatService;
import backend.academy.scrapper.repositories.jdbc.JdbcChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.Set;

@AllArgsConstructor
@ConditionalOnProperty(prefix = "app", name = "database-access-type", havingValue = "jdbc")
public class JdbcChatService implements ChatService {

    private final JdbcChatRepository jdbcChatRepository;


    @Override
    public void registerChat(Long chatId) {
        jdbcChatRepository.add(chatId);
    }

    @Override
    public boolean isChatRegistered(Long chatId) {
        if (jdbcChatRepository.getByChatId(chatId) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Chat getChatById(Long chatId) {
        return jdbcChatRepository.getByChatId(chatId);
    }

    @Override
    public void removeChat(Long chatId) {
        jdbcChatRepository.remove(chatId);
    }

    @Override
    public Set<Chat> getAllRegisteredChatIds() {
        return jdbcChatRepository.getAll();
    }
}
