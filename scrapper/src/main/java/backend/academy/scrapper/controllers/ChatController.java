package backend.academy.scrapper.controllers;

import backend.academy.scrapper.services.ChatService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/tg-chat")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    // Зарегистрировать чат
    @PostMapping("/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable Long id) {
        // Валидация ID
        if (id == null || id <= 0) {
            log.info("Недопустимый ID: {}", sanitize(id));
            return ResponseEntity.badRequest().build();
        }

        String sanitizedId = sanitize(id);
        log.info("Регистрация нового чата с ID {}", sanitizedId);

        chatService.registerChat(id);

        log.info("Чат с ID {} успешно зарегистрирован", sanitizedId);
        return ResponseEntity.ok().build();
    }

    // Удалить чат
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeChat(@PathVariable Long id) {
        if (id == null || id <= 0) {
            log.info("Попытка удаления с недопустимым ID: {}", sanitize(id));
            return ResponseEntity.badRequest().build();
        }

        String sanitizedId = sanitize(id);
        log.info("Удаление чата с ID: {}", sanitizedId);

        chatService.removeChat(id);

        log.info("Чат с ID {} успешно удален", sanitizedId);
        return ResponseEntity.ok().build();
    }

    // Метод для санитизации данных перед логгированием
    private String sanitize(Long value) {
        return value != null ? value.toString().replaceAll("[\r\n]", "") : "null";
    }
}
