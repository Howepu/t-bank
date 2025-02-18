package backend.academy.scrapper.stackoverflow;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StackOverflowClient {

    private final WebClient webClient;

    public StackOverflowClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.stackexchange.com/2.3")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    // Получаем данные о вопросе по ID
    public Mono<StackOverflowQuestion> getQuestion(String questionId) {
        return webClient.get()
            .uri("/questions/{id}?site=stackoverflow", questionId)
            .retrieve()
            .bodyToMono(StackOverflowResponse.class)
            .map(response -> response.items().get(0));
    }
}
