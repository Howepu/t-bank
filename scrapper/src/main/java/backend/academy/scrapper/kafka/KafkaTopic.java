package backend.academy.scrapper.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopic {

    @Value("app.kafka-topic-updates")
    private String updates;

    @Bean
    public NewTopic updates() {
        return TopicBuilder.name("updates").build();
    }
}
