package backend.academy.scrapper.controllers;

import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.LinkService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/links")
@AllArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @GetMapping
    public ResponseEntity<?> getAllLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        return linkService.getAllLinks(chatId);
    }

    @PostMapping
    public ResponseEntity<?> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody Link link) {
        return linkService.addLink(chatId, link);
    }

    @DeleteMapping
    public ResponseEntity<?> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        return linkService.removeLink(chatId, removeLinkRequest.url());
    }

}
