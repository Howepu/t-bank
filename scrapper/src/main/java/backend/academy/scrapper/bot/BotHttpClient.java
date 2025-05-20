package backend.academy.scrapper.bot;

import backend.academy.scrapper.dto.LinkUpdate;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class BotHttpClient {

    private final WebClient webClient;

    public BotHttpClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://bot:8080").build();
    }

    @Retry(name = "UpdateRetry", fallbackMethod = "fallbackVoid")
    @CircuitBreaker(name = "UpdateCB", fallbackMethod = "fallbackVoid")
    public Mono<Void> sendUpdate(LinkUpdate linkUpdate) {
        return webClient.post().uri("/updates").bodyValue(linkUpdate).retrieve().bodyToMono(Void.class);
    }

    public Mono<Void> fallbackVoid(LinkUpdate linkUpdate, Throwable ex) {
        log.warn("Fallback (Void): {}", ex.getMessage());
        return Mono.empty();
    }
}
