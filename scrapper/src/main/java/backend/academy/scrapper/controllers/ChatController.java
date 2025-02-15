package backend.academy.scrapper.controllers;

import backend.academy.scrapper.services.ChatService;
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

    private final ChatService chatService;

    // Зарегистрировать чат
    @PostMapping("/{id}")
    public ResponseEntity<?> registerChat(@PathVariable Long id) {
        return chatService.registerChat(id);
    }

    // Удалить чат
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeChat(@PathVariable Long id) {
        return chatService.removeChat(id);
    }

}
