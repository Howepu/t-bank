package backend.academy.scrapper.controllers;

import backend.academy.scrapper.services.ChatService;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/tg-chat")
public class ChatController {

    private static final Logger log = Logger.getLogger(ChatController.class.getName());
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
