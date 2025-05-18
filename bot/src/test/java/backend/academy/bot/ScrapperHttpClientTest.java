// package backend.academy.bot;
//
// import backend.academy.bot.clients.ScrapperHttpClient;
// import backend.academy.bot.dto.ListLinksResponse;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.web.reactive.function.client.WebClient;
// import reactor.core.publisher.Mono;
//
// import static org.mockito.Mockito.*;
//
// @SpringBootTest
// public class ScrapperHttpClientTest {
//
//    @Autowired
//    private ScrapperHttpClient scrapperHttpClient;
//
//    @MockBean
//    private WebClient webClient;  // Мокаем WebClient
//
//    @Test
//    public void testRetryMechanism() {
//        // Мокаем вызов WebClient, чтобы он вызывал ошибку
//        when(webClient.get().uri("/links").retrieve().bodyToMono(ListLinksResponse.class))
//            .thenReturn(Mono.error(new RuntimeException("Temporary error")));
//
//        // Пытаемся вызвать метод, который должен повторить попытку
//        scrapperHttpClient.getLinks(123L).subscribe(
//            response -> {
//                // Это не должно быть вызвано, так как в случае ошибки будет fallback
//            },
//            throwable -> {
//                // Ожидаем ошибку
//                assert(throwable instanceof RuntimeException);
//                verify(webClient, times(3)).get(); // Должно быть сделано 3 попытки
//            }
//        );
//    }
// }
