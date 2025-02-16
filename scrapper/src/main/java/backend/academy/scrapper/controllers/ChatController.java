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
    public ResponseEntity<Void> registerChat(@PathVariable Long id) {
        chatService.registerChat(id);
        return ResponseEntity.ok().build();
    }

    // Удалить чат
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeChat(@PathVariable Long id) {
        chatService.removeChat(id);
        return ResponseEntity.ok().build();
    }

}
