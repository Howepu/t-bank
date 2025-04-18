package backend.academy.bot.services;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import backend.academy.bot.BotApplication;
import backend.academy.bot.clients.TelegramClient;
import backend.academy.bot.dto.LinkUpdate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.TestcontainersConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {BotApplication.class, TestcontainersConfiguration.class})
@Testcontainers
@Import(TestcontainersConfiguration.class)
class KafkaBotServiceTest {

    @MockBean
    private TelegramClient telegramClient;

    @Autowired
    private KafkaTemplate<String, LinkUpdate> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        System.setProperty("TELEGRAM_TOKEN", "test-token"); // Заглушка для тестов
        doNothing().when(telegramClient).sendMessage(anyLong(), anyString());
    }

    @Test
    void testValidLinkUpdateMessageProcessed() throws Exception {
        LinkUpdate validUpdate =
                new LinkUpdate(123L, "https://github.com/user/repo", "New PR: Fix bug", List.of(123456L));

        String json = objectMapper.writeValueAsString(validUpdate);
        System.out.println("Sending JSON: " + json);

        kafkaTemplate.send("app.kafka-topic-updates", validUpdate);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() ->
                        verify(telegramClient).sendMessage(eq(123456L), contains("https://github.com/user/repo")));
    }

    @Test
    void testInvalidMessageGoesToDLQ() {

        LinkUpdate invalidUpdate = new LinkUpdate();
        invalidUpdate.url("invalid data");

        kafkaTemplate.send("app.kafka-topic-updates", invalidUpdate);

        Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> verifyNoInteractions(telegramClient));
    }
}
