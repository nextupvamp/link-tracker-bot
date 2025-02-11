package backend.academy.scrapper.service;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

public class BotClient {
    private final WebClient webClient;

    public BotClient(String botUrl) {
        webClient = WebClient.create(botUrl);
    }

    public void sendUpdate(LinkUpdate update) {
        webClient
            .post()
            .uri("/updates")
            .body(BodyInserters.fromValue(update))
            .retrieve()
            .bodyToMono(String.class)
            .block();
        // add 4xx and 5xx response handling
    }
}
