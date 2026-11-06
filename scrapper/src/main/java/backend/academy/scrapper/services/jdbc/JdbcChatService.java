package backend.academy.scrapper.services.jdbc;

import backend.academy.scrapper.models.Chat;
import backend.academy.scrapper.repositories.jdbc.JdbcChatRepository;
import backend.academy.scrapper.services.ChatService;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

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
        return jdbcChatRepository.getByChatId(chatId) != null;
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

    @Override
    public void updateMode(Long chatId, String mode) {
        jdbcChatRepository.updateMode(chatId, mode);
    }

    @Override
    public Set<Chat> getChatsByMode(String mode) {
        return jdbcChatRepository.getChatsByMode(mode);
    }
}
