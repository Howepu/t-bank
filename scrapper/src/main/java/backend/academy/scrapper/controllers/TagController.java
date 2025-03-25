package backend.academy.scrapper.controllers;

import backend.academy.scrapper.dto.ListTagsResponse;
import backend.academy.scrapper.dto.TagRequest;
import backend.academy.scrapper.dto.TagResponse;
import backend.academy.scrapper.services.TagService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<ListTagsResponse> getAllTags(@RequestHeader("Tg-Chat-Id") Long chatId) {

        if (chatId == null) {
            throw new IllegalArgumentException("Chat ID должен быть предоставлен и не может быть пустым");
        }

        String sanitizedChatId = String.valueOf(chatId).replaceAll("[\r\n]", "");
        log.info("Запрос всех ссылок для чата {}", sanitizedChatId);

        List<TagResponse> tags = tagService.findAll(chatId).stream()
                .map(tag -> new TagResponse(tag.chatId(), tag.name()))
                .toList();

        log.info("Найдено {} тэгов для чата {}", tags.size(), sanitizedChatId);
        return ResponseEntity.ok(new ListTagsResponse(tags, tags.size()));
    }

    @PostMapping
    public ResponseEntity<Void> addTag(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody TagRequest tag) {

        tagService.saveByName(tag.name(), chatId);

        return ResponseEntity.ok().build();
    }
}
