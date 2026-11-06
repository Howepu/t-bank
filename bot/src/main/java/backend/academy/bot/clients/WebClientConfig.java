package backend.academy.bot.clients;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    @Value("${spring.webclient.connect-timeout:5000}")
    private long connectTimeout;

    @Value("${spring.webclient.read-timeout:5000}")
    private long readTimeout;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(
                        new ReactorClientHttpConnector(HttpClient.create().tcpConfiguration(tcpClient -> tcpClient
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout)
                                .doOnConnected(conn -> conn.addHandlerLast(
                                        new ReadTimeoutHandler((int) readTimeout, TimeUnit.MILLISECONDS))))))
                .build();
    }
}
