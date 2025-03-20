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
            log.info("Недопустимый ID: " + id);
            return ResponseEntity.badRequest().build();
        }

        log.info("Регистрация нового чата с ID " + id);

        chatService.registerChat(id);

        log.info("Чат с ID {} успешно зарегистрирован " + id);
        return ResponseEntity.ok().build();
    }

    // Удалить чат
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeChat(@PathVariable Long id) {

        if (id == null || id <= 0) {
            log.info("Недопустимый ID: " + id);
            return ResponseEntity.badRequest().build();
        }

        log.info("Удаление чата с ID: " + id);

        chatService.removeChat(id);

        log.info("Чат с ID {} успешно удален " + id);
        return ResponseEntity.ok().build();
    }
}
