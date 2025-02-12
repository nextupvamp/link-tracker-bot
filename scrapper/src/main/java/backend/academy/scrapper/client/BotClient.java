package backend.academy.scrapper.client;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class BotClient {
    private static final String UPDATES_PATH = "/updates";

    private final WebClient webClient;

    public BotClient(String botUrl) {
        webClient = WebClient.create(botUrl);
        webClient = WebClient.builder()
            .baseUrl(botUrl)
            .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
            .build();
    }

    public void sendUpdate(LinkUpdate update) {
        webClient
            .post()
            .uri("/updates")
            .body(BodyInserters.fromValue(update))
            .uri(UPDATES_PATH)
            .bodyValue(update)
            .retrieve()
            .bodyToMono(String.class)
            .block();
        // add 4xx and 5xx response handling
    private Mono<ClientResponse> renderApiErrorResponse(ClientResponse clientResponse) {
        if (clientResponse.statusCode().isError()) {
            LOG.atInfo().addKeyValue("api_error_response", clientResponse.statusCode()).log();
            return clientResponse.bodyToMono(ApiErrorResponse.class)
                .flatMap(_ -> Mono.error(new ResponseStatusException(
                    clientResponse.statusCode()
                )));
        }
        return Mono.just(clientResponse);
    }
    }
}
