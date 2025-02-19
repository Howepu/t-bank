package backend.academy.scrapper.controllers;

import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.LinkService;
import java.util.List;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/links")
@AllArgsConstructor
public class LinkController {

    private final LinkService linkService;
    private static final Logger log = Logger.getLogger(LinkController.class.getName());

    @GetMapping
    public ResponseEntity<ListLinksResponse> getAllLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        // Санитизируем chatId перед логированием
        String sanitizedChatId = String.valueOf(chatId).replaceAll("[\r\n]", "");
        log.info("Запрос всех ссылок для чата " + sanitizedChatId);

        List<LinkResponse> links = linkService.getAllLinks(chatId).stream()
                .map(link -> new LinkResponse(link.chatId(), link.url(), link.tags(), link.filters()))
                .toList();

        log.info("Найдено " + links.size() + " ссылок для чата " + sanitizedChatId);
        return ResponseEntity.ok(new ListLinksResponse(links, links.size()));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody Link link) {
        // Санитизируем URL и chatId перед логированием
        String sanitizedUrl = link.url().replaceAll("[\r\n]", "");
        String sanitizedChatId = String.valueOf(chatId).replaceAll("[\r\n]", "");
        log.info("Добавление новой ссылки: " + sanitizedUrl + " для чата " + sanitizedChatId);

        Link savedLink = linkService.addLink(chatId, link);
        log.info("Ссылка успешно добавлена: " + sanitizedUrl + " для чата " + sanitizedChatId);

        return ResponseEntity.ok(
                new LinkResponse(savedLink.chatId(), savedLink.url(), savedLink.tags(), savedLink.filters()));
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        // Санитизируем URL и chatId перед логированием
        String sanitizedUrl = removeLinkRequest.url().replaceAll("[\r\n]", "");
        String sanitizedChatId = String.valueOf(chatId).replaceAll("[\r\n]", "");
        log.info("Запрос на удаление ссылки: " + sanitizedUrl + " для чата " + sanitizedChatId);

        Link link = linkService.removeLink(chatId, removeLinkRequest.url());

        // Убираем проверку на null, так как метод removeLink не должен возвращать null
        log.info("Ссылка успешно удалена: " + sanitizedUrl + " для чата " + sanitizedChatId);
        return ResponseEntity.ok(new LinkResponse(link.chatId(), link.url(), link.tags(), link.filters()));
    }
}
