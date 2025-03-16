package backend.academy.scrapper.bot;

import backend.academy.scrapper.dto.LinkUpdate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class BotHttpClient {

    private final WebClient webClient;

    public BotHttpClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public Mono<Void> sendUpdate(LinkUpdate linkUpdate) {
        return webClient.post()
            .uri("/updates")
            .bodyValue(linkUpdate)
            .retrieve()
            .bodyToMono(Void.class);
    }
}
