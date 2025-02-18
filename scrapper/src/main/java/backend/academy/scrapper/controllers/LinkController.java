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
        log.info("Запрос всех ссылок для чата {}", chatId);

        List<LinkResponse> links = linkService.getAllLinks(chatId).stream()
            .map(link -> new LinkResponse(link.chatId(), link.url(), link.tags(), link.filters()))
            .toList();

        log.info("Найдено {} ссылок для чата {}", links.size(), chatId);
        return ResponseEntity.ok(new ListLinksResponse(links, links.size()));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody Link link) {
        log.info("Добавление новой ссылки: {} для чата {}", link.url(), chatId);

        Link savedLink = linkService.addLink(chatId, link);
        log.info("Ссылка успешно добавлена: {} для чата {}", savedLink.url(), chatId);

        return ResponseEntity.ok(new LinkResponse(savedLink.chatId(), savedLink.url(), savedLink.tags(), savedLink.filters()));
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        log.info("Запрос на удаление ссылки: {} для чата {}", removeLinkRequest.url(), chatId);

        Link link = linkService.removeLink(chatId, removeLinkRequest.url());

        if (link == null) {
            log.warn("Попытка удалить несуществующую ссылку: {} для чата {}", removeLinkRequest.url(), chatId);
            return ResponseEntity.notFound().build();
        }

        log.info("Ссылка успешно удалена: {} для чата {}", link.url(), chatId);
        return ResponseEntity.ok(new LinkResponse(link.chatId(), link.url(), link.tags(), link.filters()));
    }

}
