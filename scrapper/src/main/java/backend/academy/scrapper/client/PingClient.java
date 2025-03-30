package backend.academy.scrapper.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PingClient {
    private final WebClient webClient;

    public PingClient(WebClient.Builder builder) {
        webClient = builder.build();
    }

    public boolean ping(String url) {
        try {
            webClient.get().uri(url).retrieve().bodyToMono(String.class).block();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
