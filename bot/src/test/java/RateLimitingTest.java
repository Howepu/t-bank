// package backend.academy.bot;
//
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.client.reactive.ReactorClientHttpConnector;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.web.reactive.function.client.WebClient;
// import reactor.netty.http.client.HttpClient;
//
// import java.util.concurrent.atomic.AtomicInteger;
//
// import static org.junit.jupiter.api.Assertions.*;
//
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) // порт 8080
// @ActiveProfiles("test") // если у вас есть отдельный конфиг
// public class RateLimitingTest {
//
//    private final WebClient webClient = WebClient.builder()
//        .baseUrl("http://localhost:8081")
//        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
//        .build();
//
//    @Test
//    void rateLimitingShouldReturn429AfterTooManyRequests() {
//        int totalRequests = 20;
//        AtomicInteger status429Count = new AtomicInteger();
//
//        for (int i = 0; i < totalRequests; i++) {
//            webClient
//                .get()
//                .uri("/links")
//                .header("Tg-Chat-Id", "12345")
//                .exchangeToMono(response -> {
//                    if (response.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
//                        status429Count.incrementAndGet();
//                    }
//                    return response.bodyToMono(Void.class);
//                })
//                .block(); // блокируем выполнение до получения ответа
//        }
//
//        System.out.println("429 responses: " + status429Count.get());
//        assertTrue(status429Count.get() > 0, "Should receive at least one 429 Too Many Requests");
//    }
// }
