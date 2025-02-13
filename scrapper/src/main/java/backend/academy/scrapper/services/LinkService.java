package backend.academy.scrapper.services;

import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.models.Link;
import backend.academy.scrapper.repositories.LinkRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;

    public LinkResponse addLink(Long chatId, Link link) {
        Link savedLink = linkRepository.save(chatId, link);

        return new LinkResponse(savedLink.chatId(), savedLink.url(), savedLink.tags(), savedLink.filters());
    }

    public ResponseEntity<?> removeLink(Long chatId, String url) {
        Link link = linkRepository.remove(chatId, url);

        if (link == null) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse();
            return ResponseEntity.status(404).body((apiErrorResponse));
        }

        return ResponseEntity.ok().body(link);
    }

    public List<LinkResponse> getAllLinks(Long chatId) {
        List<Link> savedLinks =  linkRepository.findAllByChatId(chatId);

        return savedLinks
            .stream()
            .map(link ->
                new LinkResponse(link.chatId(), link.url(), link.tags(), link.filters())
            )
            .toList();
    }

}
