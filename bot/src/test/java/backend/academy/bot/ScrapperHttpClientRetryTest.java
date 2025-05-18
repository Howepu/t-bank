// package backend.academy.bot;
//
// import backend.academy.bot.clients.ScrapperHttpClient;
// import com.github.tomakehurst.wiremock.client.WireMock;
// import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
// import com.pengrad.telegrambot.TelegramBot;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.RegisterExtension;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.springframework.test.web.reactive.server.WebTestClient;
//
// import static com.github.tomakehurst.wiremock.client.WireMock.*;
// import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
//
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
// @AutoConfigureWebTestClient
// public class ScrapperHttpClientRetryTest {
//
//    @RegisterExtension
//    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
//        .options(wireMockConfig().port(8081))  // Убедитесь, что WireMock использует правильный порт
//        .build();
//
//    @DynamicPropertySource
//    static void setUpMockBaseUrl(DynamicPropertyRegistry registry) {
//        registry.add("scrapper.base-url", () -> "http://localhost:8081");  // Использование правильного порта
//    }
//
//    @MockBean
//    private TelegramBot telegramBot;
//
//    @Autowired
//    private WebTestClient client;
//
//    @Autowired
//    private ScrapperHttpClient scrapperHttpClient;
//
//    @BeforeEach
//    void setup() {
//        wireMockExtension.resetAll();
//    }
//
//    @Test
//    void shouldRetryOnServerError() {
//        // Стабы с правильным адресом
//        wireMockExtension.stubFor(WireMock.get("/links")
//            .willReturn(aResponse()
//                .withStatus(502)));  // Первый сбой (502 Bad Gateway)
//
//        wireMockExtension.stubFor(WireMock.get("/links")
//            .willReturn(aResponse()
//                .withStatus(505)));  // Второй сбой (505 HTTP Version Not Supported)
//
//        wireMockExtension.stubFor(WireMock.get("/links")
//            .willReturn(aResponse()
//                .withStatus(200)
//                .withHeader("Content-Type", "application/json")
//                .withBody("{\"links\": [], \"size\": 0}")));  // Успешный ответ после нескольких попыток
//
//        // Проверка, что ScrapperHttpClient делает 3 попытки
//        scrapperHttpClient.getLinks(123L)
//            .doOnTerminate(() -> {
//                // Мы ожидаем, что после нескольких неудачных попыток будет вызван успешный ответ
//                wireMockExtension.verify(3, getRequestedFor(urlEqualTo("/links")));
//            })
//            .block();  // Дожидаемся завершения
//
//        // Тестовый запрос с использованием baseUrl WireMock для подтверждения успешного ответа
//        client.get().uri("/links").exchange()
//            .expectStatus().isOk()
//            .expectBody().json("{\"links\": [], \"size\": 0}");
//    }
// }
//
