package backend.academy.scrapper.client;

import backend.academy.scrapper.service.LinkUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class BotClient {
    private static final Logger LOG = LoggerFactory.getLogger(BotClient.class);
    private static final String UPDATES_PATH = "/updates";

    private final WebClient webClient;

    public BotClient(String botUrl) {
        webClient = WebClient.builder()
                .baseUrl(botUrl)
                .filter(logRequest())
                .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
                .filter(logResponse())
                .build();
    }

    public void sendUpdate(LinkUpdate update) {
        webClient
                .post()
                .uri(UPDATES_PATH)
                .bodyValue(update)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private Mono<ClientResponse> renderApiErrorResponse(ClientResponse clientResponse) {
        return ClientUtils.renderApiErrorResponse(clientResponse, LOG);
    }

    private ExchangeFilterFunction logRequest() {
        return ClientUtils.logRequest(LOG);
    }

    private ExchangeFilterFunction logResponse() {
        return ClientUtils.logResponse(LOG);
    }
}
