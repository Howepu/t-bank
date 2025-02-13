package backend.academy.scrapper.controllers;

import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.LinkService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/links")
@AllArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @GetMapping
    public ResponseEntity<List<LinkResponse>> getAllLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        List<LinkResponse> links = linkService.getAllLinks(chatId);
        return ResponseEntity.ok(links);
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody Link link) {
        LinkResponse savedLink = linkService.addLink(chatId, link);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLink);
    }

    @DeleteMapping
    public ResponseEntity<?> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        return linkService.removeLink(chatId, removeLinkRequest.url());
    }

}
