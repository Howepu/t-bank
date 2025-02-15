package backend.academy.scrapper.services;

import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.dto.ListLinksResponse;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.repositories.LinkRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final ChatService chatService;

    public ResponseEntity<?> addLink(Long chatId, Link link) {
        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }
        // Проверка на корректность параметров
        if (chatId == null) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            apiErrorResponse.description("Некорректные параметры запроса");
            apiErrorResponse.code("400");
            apiErrorResponse.exceptionName("InvalidRequest");
            apiErrorResponse.exceptionMessage("Chat ID должен быть предоставлен и не может быть пустым");
            return ResponseEntity.status(400).body(apiErrorResponse);
        }

        Link savedLink = linkRepository.save(chatId, link);
        LinkResponse linkResponse = new LinkResponse(savedLink.chatId(), savedLink.url(), savedLink.tags(),
            savedLink.filters());

        return ResponseEntity.ok().body(linkResponse);
    }

    public ResponseEntity<?> removeLink(Long chatId, String url) {

        if (!chatService.isChatRegistered(chatId)) {
            throw new IllegalStateException("Чат не зарегистрирован");
        }

        // Проверка на корректность параметров
        if (chatId == null || url == null || url.isEmpty()) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            apiErrorResponse.description("Некорректные параметры запроса");
            apiErrorResponse.code("400");
            apiErrorResponse.exceptionName("InvalidRequest");
            apiErrorResponse.exceptionMessage("Chat ID и URL должны быть предоставлены и не могут быть пустыми");
            return ResponseEntity.status(400).body(apiErrorResponse);
        }

        // Попытка удалить ссылку
        Link link = linkRepository.remove(chatId, url);

        if (link == null) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            apiErrorResponse.description("Ссылка не найдена");
            apiErrorResponse.code("404");
            apiErrorResponse.exceptionName("Not Found");
            apiErrorResponse.exceptionMessage("Такой link отсутствует");
            return ResponseEntity.status(404).body((apiErrorResponse));
        }

        LinkResponse linkResponse = new LinkResponse(link.chatId(), link.url(), link.tags(), link.filters());
        return ResponseEntity.ok().body(linkResponse);
    }

    public ResponseEntity<?> getAllLinks(Long chatId) {


        // Проверка на корректность параметров
        if (chatId == null) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            apiErrorResponse.description("Некорректные параметры запроса");
            apiErrorResponse.code("400");
            apiErrorResponse.exceptionName("InvalidRequest");
            apiErrorResponse.exceptionMessage("Chat ID должен быть предоставлен и не может быть пустым");
            return ResponseEntity.status(400).body(apiErrorResponse);
        }

        List<Link> savedLinks = linkRepository.findAllByChatId(chatId);
        List<LinkResponse> links = savedLinks.stream()
            .map(link -> new LinkResponse(link.chatId(), link.url(), link.tags(), link.filters()))
            .toList();

        ListLinksResponse listLinksResponse = new ListLinksResponse(links, links.size());
        return ResponseEntity.ok(listLinksResponse);
    }
}
