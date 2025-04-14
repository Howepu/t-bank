package backend.academy.scrapper.controllers;

import backend.academy.scrapper.dto.*;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.LinkService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/links")
public class LinkController {

    private final LinkService linkService;

    @GetMapping
    public ResponseEntity<ListLinksResponse> getAllLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        String sanitizedChatId = sanitize(chatId);
        log.info("Запрос всех ссылок для чата {}", sanitizedChatId);

        List<LinkResponse> links = linkService.getAllLinks(chatId).stream()
                .map(link -> new LinkResponse(
                        link.chat().chatId(), sanitize(link.url()), link.assignedTags(), link.assignedFilters()))
                .toList();

        log.info("Найдено {} ссылок для чата {}", links.size(), sanitizedChatId);
        return ResponseEntity.ok(new ListLinksResponse(links, links.size()));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody Link link) {
        String sanitizedUrl = sanitize(link.url());
        String sanitizedChatId = sanitize(chatId);
        log.info("Добавление новой ссылки: {} для чата {}", sanitizedUrl, sanitizedChatId);

        Link savedLink = linkService.addLink(chatId, link);
        log.info("Ссылка успешно добавлена: {} для чата {}", sanitizedUrl, sanitizedChatId);

        return ResponseEntity.ok(
                new LinkResponse(chatId, savedLink.url(), savedLink.assignedTags(), savedLink.assignedFilters()));
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        String sanitizedUrl = sanitize(removeLinkRequest.url());
        String sanitizedChatId = sanitize(chatId);
        log.info("Запрос на удаление ссылки: {} для чата {}", sanitizedUrl, sanitizedChatId);

        Link link = linkService.removeLink(chatId, removeLinkRequest.url());
        log.info("Ссылка успешно удалена: {} для чата {}", sanitizedUrl, sanitizedChatId);

        return ResponseEntity.ok(new LinkResponse(chatId, link.url(), link.assignedTags(), link.assignedFilters()));
    }

    @PostMapping("/tag")
    public ResponseEntity<ListLinkTagResponse> getLinksByTag(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody TagRequest tagRequest) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        String sanitizedTagName = sanitize(tagRequest.name());
        String sanitizedChatId = sanitize(chatId);
        log.info("Запрос всех ссылок по тегу '{}' для чата {}", sanitizedTagName, sanitizedChatId);

        List<LinkTagResponse> links = linkService.findLinksByTag(tagRequest.name(), chatId).stream()
                .map(link -> new LinkTagResponse(sanitize(link.url())))
                .toList();

        log.info("Найдено {} ссылок по тегу '{}' для чата {}", links.size(), sanitizedTagName, sanitizedChatId);
        return ResponseEntity.ok(new ListLinkTagResponse(links, links.size()));
    }

    @PostMapping("/attach-tag")
    public ResponseEntity<String> attachTagToLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AttachTagRequest request) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        String sanitizedTagName = sanitize(request.tagName());
        String sanitizedUrl = sanitize(request.url());
        String sanitizedChatId = sanitize(chatId);

        linkService.attachTagToLink(request.url(), request.tagName());
        log.info("Тег '{}' привязан к ссылке '{}' для чата {}", sanitizedTagName, sanitizedUrl, sanitizedChatId);
        return ResponseEntity.ok(String.format("Тег '%s' привязан к ссылке '%s'", sanitizedTagName, sanitizedUrl));
    }

    @DeleteMapping("/attach-tag")
    public ResponseEntity<String> detachTagFromLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AttachTagRequest request) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        String sanitizedTagName = sanitize(request.tagName());
        String sanitizedUrl = sanitize(request.url());
        String sanitizedChatId = sanitize(chatId);

        linkService.detachTagFromLink(request.url(), request.tagName());
        log.info("Тег '{}' отвязан от ссылки '{}' для чата {}", sanitizedTagName, sanitizedUrl, sanitizedChatId);
        return ResponseEntity.ok(String.format("Тег '%s' отвязан от ссылки '%s'", sanitizedTagName, sanitizedUrl));
    }

    private String sanitize(String input) {
        return input != null ? input.replaceAll("[\r\n]", "") : "null";
    }

    private String sanitize(Long input) {
        return input != null ? String.valueOf(input).replaceAll("[\r\n]", "") : "null";
    }
}
