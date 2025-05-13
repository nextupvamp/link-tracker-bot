package backend.academy.scrapper.client.util;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PingClient {
    private final WebClient webClient;

    public PingClient(WebClient.Builder builder) {
        webClient = builder.build();
    }

    /** Checks if url is available at the moment */
    public boolean ping(String url) {
        try {
            webClient.get().uri(url).retrieve().bodyToMono(String.class).block();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
