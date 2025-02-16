package backend.academy.scrapper.controllers;

import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.LinkService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/links")
@AllArgsConstructor
@Slf4j
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
        log.info("Удаление ссылки: {} для чата {}", link.url(), chatId);
        Link savedLink = linkService.addLink(chatId, link);
        return ResponseEntity.ok(new LinkResponse(savedLink.chatId(), savedLink.url(), savedLink.tags(), savedLink.filters()));
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        log.info("Удаление ссылки: {} для чата {}", removeLinkRequest.url(), chatId);

        Link link = linkService.removeLink(chatId, removeLinkRequest.url());

        if (link == null) {
            log.warn("Ссылка {} не найдена для чата {}", removeLinkRequest.url(), chatId);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new LinkResponse(link.chatId(), link.url(), link.tags(), link.filters()));
    }

}

