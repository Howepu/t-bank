package backend.academy.scrapper.github;

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GitHubClient {

    private final WebClient webClient;

    public GitHubClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.github.com")
                .defaultHeaders(h -> h.setBearerAuth(System.getenv("GITHUB_TOKEN")))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<GitHubRepository> getRepository(String owner, String repo) {
        return webClient
                .get()
                .uri("/repos/{owner}/{repo}", owner, repo)
                .retrieve()
                .bodyToMono(GitHubRepository.class);
    }

    public Mono<List<GitHubIssuePullRequest>> getIssues(String owner, String repo) {
        // Пример запроса к GitHub API для получения списка Issues
        return webClient
                .get()
                .uri("https://api.github.com/repos/{owner}/{repo}/issues", owner, repo)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GitHubIssuePullRequest>>() {});
    }

    public Mono<List<GitHubIssuePullRequest>> getPullRequests(String owner, String repo) {
        // Пример запроса к GitHub API для получения списка Pull Requests
        return webClient
                .get()
                .uri("https://api.github.com/repos/{owner}/{repo}/pulls", owner, repo)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<GitHubIssuePullRequest>>() {});
    }
}
