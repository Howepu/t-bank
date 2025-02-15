package backend.academy.scrapper.services;

import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.repositories.InMemoryChatRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ChatService {

    private final InMemoryChatRepository chatRepository;

    public ResponseEntity<?> registerChat(Long chatId) {
        if (chatId == null) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            apiErrorResponse.description("Некорректные параметры запроса");
            apiErrorResponse.code("400");
            apiErrorResponse.exceptionName("InvalidRequest");
            apiErrorResponse.exceptionMessage("Chat ID должен быть предоставлен и не может быть пустым");
            return ResponseEntity.status(400).body(apiErrorResponse);
        }
        chatRepository.save(chatId);
        return ResponseEntity.ok().build();
    }

    public boolean isChatRegistered(Long chatId) {
        return chatRepository.isChatRegistered(chatId);
    }

    public ResponseEntity<?> removeChat(Long chatId) {

        if (chatId == null) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            apiErrorResponse.description("Некорректные параметры запроса");
            apiErrorResponse.code("400");
            apiErrorResponse.exceptionName("InvalidRequest");
            apiErrorResponse.exceptionMessage("Chat ID должен быть предоставлен и не может быть пустым");
            return ResponseEntity.status(400).body(apiErrorResponse);
        }

        if (!chatRepository.isChatRegistered(chatId)) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            apiErrorResponse.description("Чат не найден");
            apiErrorResponse.code("404");
            apiErrorResponse.exceptionName("ChatNotFound");
            apiErrorResponse.exceptionMessage("Чат с ID " + chatId + " не существует");
            return ResponseEntity.status(404).body(apiErrorResponse);
        }

        // Удаление чата
        chatRepository.remove(chatId);
        return ResponseEntity.ok().build();
    }

}
