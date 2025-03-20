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
        this.webClient = webClientBuilder
                .baseUrl("https://api.stackexchange.com/2.3")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    // Получаем данные о вопросе по ID
    public Mono<StackOverflowQuestionAnswer> getQuestion(String questionId) {
        return webClient
                .get()
                .uri("/questions/{id}?site=stackoverflow", questionId)
                .retrieve()
                .bodyToMono(StackOverflowResponse.class)
                .map(response -> response.items().get(0));
    }

    public Mono<StackOverflowQuestionAnswer> getLatestAnswer(String questionId) {
        return webClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/questions/{id}/answers")
                .queryParam("site", "stackoverflow")
                .queryParam("order", "desc")
                .queryParam("sort", "creation")
                .queryParam("filter", "!6Wfm(gIbInW9a") // Фильтр с body
                .queryParam("pagesize", 1)
                .build(questionId))
            .retrieve()
            .bodyToMono(StackOverflowResponse.class)
            .map(response -> response.items().isEmpty() ? null : response.items().get(0));
    }
}
