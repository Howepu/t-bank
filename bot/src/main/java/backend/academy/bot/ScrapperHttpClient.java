package backend.academy.bot;


import backend.academy.bot.models.AddLinkRequest;
import backend.academy.bot.models.LinkResponse;
import backend.academy.bot.models.ListLinksResponse;
import backend.academy.bot.models.RemoveLinkRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ScrapperHttpClient {

    private final WebClient webClient;

    public ScrapperHttpClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public Mono<Void> registerChat(long id) {
        return webClient.post()
            .uri("/tg-chat/{id}", id)
            .retrieve()
            .bodyToMono(Void.class);
    }

    public Mono<Void> deleteChat(long id) {
        return webClient.delete()
            .uri("/tg-chat/{id}", id)
            .retrieve()
            .bodyToMono(Void.class);
    }

    public Mono<ListLinksResponse> getLinks(long id) {
        return webClient.get()
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(id))
            .retrieve()
            .bodyToMono(ListLinksResponse.class);
    }

    public Mono<LinkResponse> addLink(long id, AddLinkRequest request) {
        return webClient.post()
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(id))
            .bodyValue(request)
            .retrieve()
            .bodyToMono(LinkResponse.class);

    }

    public Mono<LinkResponse> removeLink(long id, RemoveLinkRequest request) {
        return webClient.delete()
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(id))
            .retrieve()
            .bodyToMono(LinkResponse.class);
    }
}
