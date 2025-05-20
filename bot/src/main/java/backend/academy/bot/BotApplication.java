package backend.academy.bot;

import io.github.resilience4j.retry.autoconfigure.RetryAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(exclude = {RetryAutoConfiguration.class})
@EnableConfigurationProperties({BotConfig.class})
@PropertySource(value = "classpath:application.yml", ignoreResourceNotFound = true)
@EnableRetry
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
