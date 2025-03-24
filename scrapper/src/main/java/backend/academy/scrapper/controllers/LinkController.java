package backend.academy.scrapper.controllers;

import backend.academy.scrapper.dto.AttachTagRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.LinkTagResponse;
import backend.academy.scrapper.dto.ListLinkTagResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.dto.TagRequest;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.services.LinkService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

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

        String sanitizedChatId = String.valueOf(chatId).replaceAll("[\r\n]", "");
        log.info("Запрос всех ссылок для чата {}", sanitizedChatId);

        List<LinkResponse> links = linkService.getAllLinks(chatId).stream()
            .map(link -> new LinkResponse(
                link.chat().chatId(),
                link.url(),
                link.assignedTags(),
                link.filters()
            ))
            .toList();

        log.info("Найдено {} ссылок для чата {}", links.size(), sanitizedChatId);
        return ResponseEntity.ok(new ListLinksResponse(links, links.size()));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody Link link) {
        // Санитизируем URL и chatId перед логированием
        String sanitizedUrl = link.url().replaceAll("[\r\n]", "");
        String sanitizedChatId = String.valueOf(chatId).replaceAll("[\r\n]", "");
        log.info("Добавление новой ссылки: {} для чата {}", sanitizedUrl, sanitizedChatId);

        Link savedLink = linkService.addLink(chatId, link);
        log.info("Ссылка успешно добавлена: {} для чата {}", sanitizedUrl, sanitizedChatId);

        return ResponseEntity.ok(
            new LinkResponse(chatId, savedLink.url(), savedLink.assignedTags(), savedLink.filters()));
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(
        @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody RemoveLinkRequest removeLinkRequest) {
        // Санитизируем URL и chatId перед логированием
        String sanitizedUrl = removeLinkRequest.url().replaceAll("[\r\n]", "");
        String sanitizedChatId = String.valueOf(chatId).replaceAll("[\r\n]", "");
        log.info("Запрос на удаление ссылки: {} для чата {}", sanitizedUrl, sanitizedChatId);

        Link link = linkService.removeLink(chatId, removeLinkRequest.url());

        // Убираем проверку на null, так как метод removeLink не должен возвращать null
        log.info("Ссылка успешно удалена: {} для чата {}", sanitizedUrl, sanitizedChatId);
        return ResponseEntity.ok(new LinkResponse(chatId, link.url(), link.assignedTags(), link.filters()));
    }

    @PostMapping("/tag")
    public ResponseEntity<ListLinkTagResponse> getLinksByTag(
        @RequestHeader("Tg-Chat-Id") Long chatId,
        @RequestBody TagRequest tagRequest
    ) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        String tagName = tagRequest.name(); // Достаём имя тега из DTO
        log.info("Запрос всех ссылок по тегу '{}' для чата {}", tagName, chatId);

        List<LinkTagResponse> links = linkService.findLinksByTag(tagName, chatId).stream()
            .map(link -> new LinkTagResponse(
                link.url()
            ))
            .toList();

        log.info("Найдено {} ссылок по тегу '{}' для чата {}", links.size(), tagName, chatId);
        return ResponseEntity.ok(new ListLinkTagResponse(links, links.size()));
    }

    @PostMapping("/attach-tag")
    public ResponseEntity<String> attachTagToLink(
        @RequestHeader("Tg-Chat-Id") Long chatId,
        @RequestBody AttachTagRequest request
    ) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        linkService.attachTagToLink(request.url(), request.tagName());
        log.info("Тег '{}' привязан к ссылке '{}'", request.tagName(), request.url());
        return ResponseEntity.ok("Тег '" + request.tagName() + "' привязан к ссылке '" + request.url() + "'");
    }

    @DeleteMapping("/attach-tag")
    public ResponseEntity<String> detachTagFromLink(
        @RequestHeader("Tg-Chat-Id") Long chatId,
        @RequestBody AttachTagRequest request
    ) {
        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        linkService.detachTagFromLink(request.url(), request.tagName());
        log.info("Тег '{}' отвязан от ссылки '{}'", request.tagName(), request.url());
        return ResponseEntity.ok("Тег '" + request.tagName() + "' отвязан от ссылки '" + request.url() + "'");
    }
}
