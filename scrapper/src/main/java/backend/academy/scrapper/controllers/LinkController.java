package backend.academy.scrapper.controllers;

import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.LinkService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/links")
@AllArgsConstructor
public class LinkController {

    private final LinkService linkService;

    @GetMapping
    public ResponseEntity<ListLinksResponse> getAllLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        List<LinkResponse> links = linkService.getAllLinks(chatId).stream()
            .map(link -> new LinkResponse(link.chatId(), link.url(), link.tags(), link.filters()))
            .toList();

        return ResponseEntity.ok(new ListLinksResponse(links, links.size()));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody Link link) {
        Link savedLink = linkService.addLink(chatId, link);
        return ResponseEntity.ok(new LinkResponse(savedLink.chatId(), savedLink.url(), savedLink.tags(), savedLink.filters()));
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        Link link = linkService.removeLink(chatId, removeLinkRequest.url());
        return ResponseEntity.ok(new LinkResponse(link.chatId(), link.url(), link.tags(), link.filters()));
    }
}

