package backend.academy.bot.controllers;


import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.services.BotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
public class UpdateController {

    private final BotService botService;

    public UpdateController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping
    public ResponseEntity<Void> receiveUpdate(@RequestBody LinkUpdate update) {
        botService.processUpdate(update);
        return ResponseEntity.ok().build();
    }
}

