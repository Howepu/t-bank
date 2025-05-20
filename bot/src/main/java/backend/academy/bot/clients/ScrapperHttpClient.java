package backend.academy.bot.clients;

import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.AttachTagRequest;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinkTagResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.ListTagsResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import backend.academy.bot.dto.TagRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ScrapperHttpClient {

    private final WebClient webClient;

    public ScrapperHttpClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://scrapper:8081").build();
    }

    @Retry(name = "getLinksRetry", fallbackMethod = "fallbackVoid")
    @CircuitBreaker(name = "getLinksCB", fallbackMethod = "fallbackVoid")
    public Mono<Void> registerChat(long chatId) {
        return webClient.post().uri("/tg-chat/{id}", chatId).retrieve().bodyToMono(Void.class);
    }

    public Mono<Void> deleteChat(long chatId) {
        return webClient.delete().uri("/tg-chat/{id}", chatId).retrieve().bodyToMono(Void.class);
    }

    @Retry(name = "getLinksRetry", fallbackMethod = "fallbackSetChatMode")
    @CircuitBreaker(name = "getLinksCB", fallbackMethod = "fallbackSetChatMode")
    public Mono<Void> setChatMode(long chatId, String mode) {
        return webClient
                .post()
                .uri("/tg-chat/{id}/mode", chatId)
                .bodyValue(mode)
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Retry(name = "getLinksRetry", fallbackMethod = "fallbackLinks")
    @CircuitBreaker(name = "getLinksCB", fallbackMethod = "fallbackLinks")
    public Mono<ListLinksResponse> getLinks(long chatId) {
        log.info("Attempting to get links for chatId: {}", chatId);
        return webClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .bodyToMono(ListLinksResponse.class);
    }

    @Retry(name = "getLinksRetry", fallbackMethod = "fallbackAddLink")
    @CircuitBreaker(name = "getLinksCB", fallbackMethod = "fallbackAddLink")
    public Mono<LinkResponse> addLink(long chatId, AddLinkRequest request) {
        log.info("Attempting to get links for chatId: {}", chatId);
        return webClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LinkResponse.class);
    }

    @Retry(name = "getLinksRetry", fallbackMethod = "fallbackRemoveLink")
    @CircuitBreaker(name = "getLinksCB", fallbackMethod = "fallbackRemoveLink")
    public Mono<LinkResponse> removeLink(long chatId, RemoveLinkRequest request) {
        log.info("Attempting to get links for chatId: {}", chatId);
        return webClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LinkResponse.class);
    }

    @Retry(name = "getLinksRetry", fallbackMethod = "fallbackTags")
    @CircuitBreaker(name = "getLinksCB", fallbackMethod = "fallbackTags")
    public Mono<ListTagsResponse> getTags(long chatId) {
        return webClient
                .get()
                .uri("/tags")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .retrieve()
                .bodyToMono(ListTagsResponse.class);
    }

    @Retry(name = "getLinksRetry", fallbackMethod = "fallbackAddTag")
    @CircuitBreaker(name = "getLinksCB", fallbackMethod = "fallbackAddTag")
    public Mono<Void> addTag(long chatId, TagRequest request) {
        return webClient
                .post()
                .uri("/tags")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Retry(name = "getLinksRetry", fallbackMethod = "fallbackLinksByTag")
    @CircuitBreaker(name = "getLinksCB", fallbackMethod = "fallbackLinksByTag")
    public Mono<ListLinkTagResponse> getLinksByTag(long chatId, TagRequest request) {
        return webClient
                .post()
                .uri("/links/tag")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ListLinkTagResponse.class);
    }

    @Retry(name = "getLinksRetry", fallbackMethod = "fallbackAttachDetach")
    @CircuitBreaker(name = "getLinksCB", fallbackMethod = "fallbackAttachDetach")
    public Mono<String> attachTagToLink(Long chatId, AttachTagRequest request) {
        return webClient
                .post()
                .uri("/links/attach-tag")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);
    }

    @Retry(name = "getLinksRetry", fallbackMethod = "fallbackAttachDetach")
    @CircuitBreaker(name = "getLinksCB", fallbackMethod = "fallbackAttachDetach")
    public Mono<String> detachTagFromLink(long chatId, AttachTagRequest request) {
        return webClient
                .method(HttpMethod.DELETE)
                .uri("/links/attach-tag")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<Void> fallbackVoid(long chatId, Throwable ex) {
        log.warn("Fallback (Void): {}", ex.getMessage());
        return Mono.empty();
    }

    public Mono<Void> fallbackSetChatMode(long chatId, String mode, Throwable ex) {
        log.warn("Fallback (setChatMode): mode={}, error={}", mode, ex.getMessage());
        return Mono.empty();
    }

    public Mono<ListLinksResponse> fallbackLinks(long chatId, Throwable ex) {
        log.warn("Fallback (getLinks): {}", ex.getMessage());
        return Mono.just(new ListLinksResponse(List.of(), 0));
    }

    public Mono<LinkResponse> fallbackAddLink(long chatId, AddLinkRequest request, Throwable ex) {
        log.warn("Fallback (addLink): {}, error={}", request, ex.getMessage());
        return Mono.just(new LinkResponse(0, null, Set.of(), Set.of()));
    }

    public Mono<LinkResponse> fallbackRemoveLink(long chatId, RemoveLinkRequest request, Throwable ex) {
        log.warn("Fallback (removeLink): {}, error={}", request, ex.getMessage());
        return Mono.just(new LinkResponse(0, null, Set.of(), Set.of()));
    }

    public Mono<ListTagsResponse> fallbackTags(long chatId, Throwable ex) {
        log.warn("Fallback (getTags): {}", ex.getMessage());
        return Mono.just(new ListTagsResponse(List.of(), 0));
    }

    public Mono<Void> fallbackAddTag(long chatId, TagRequest request, Throwable ex) {
        log.warn("Fallback (addTag): {}, error={}", request, ex.getMessage());
        return Mono.empty();
    }

    public Mono<ListLinkTagResponse> fallbackLinksByTag(long chatId, TagRequest request, Throwable ex) {
        log.warn("Fallback (getLinksByTag): {}, error={}", request, ex.getMessage());
        return Mono.just(new ListLinkTagResponse(List.of(), 0));
    }

    public Mono<String> fallbackAttachDetach(long chatId, AttachTagRequest request, Throwable ex) {
        log.warn("Fallback (attach/detach tag): {}, error={}", request, ex.getMessage());
        return Mono.just("Fallback");
    }
}
