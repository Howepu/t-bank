package backend.academy.bot.clients;

import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.AttachTagRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinkTagResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.ListTagsResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import backend.academy.bot.dto.TagRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ScrapperHttpClient {

    private final WebClient webClient;

    public ScrapperHttpClient(WebClient.Builder webClientBuilder, @Value("${scrapper.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<Void> registerChat(long chatId) {
        return webClient.post().uri("/tg-chat/{id}", chatId).retrieve().bodyToMono(Void.class);
    }

    public Mono<Void> deleteChat(long chatId) {
        return webClient.delete().uri("/tg-chat/{id}", chatId).retrieve().bodyToMono(Void.class);
    }

    public Mono<Void> setChatMode(long chatId, String mode) {
        return webClient
                .post()
                .uri("/tg-chat/{id}/mode", chatId)
                .bodyValue(mode)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<ListLinksResponse> getLinks(long chatId) {
        return webClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .bodyToMono(ListLinksResponse.class);
    }

    public Mono<LinkResponse> addLink(long chatId, AddLinkRequest request) {
        return webClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LinkResponse.class);
    }

    public Mono<LinkResponse> removeLink(long chatId, RemoveLinkRequest request) {
        return webClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LinkResponse.class);
    }

    public Mono<ListTagsResponse> getTags(long chatId) {
        return webClient
                .get()
                .uri("/tags")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .bodyToMono(ListTagsResponse.class);
    }

    public Mono<Void> addTag(long chatId, TagRequest request) {
        return webClient
                .post()
                .uri("/tags")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<ListLinkTagResponse> getLinksByTag(long chatId, TagRequest request) {
        return webClient
                .post()
                .uri("/links/tag")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ListLinkTagResponse.class);
    }

    public Mono<String> attachTagToLink(Long chatId, AttachTagRequest request) {
        return webClient
                .post()
                .uri("/links/attach-tag")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> detachTagFromLink(long chatId, AttachTagRequest request) {
        return webClient
                .method(HttpMethod.DELETE)
                .uri("/links/attach-tag")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);
    }
}
