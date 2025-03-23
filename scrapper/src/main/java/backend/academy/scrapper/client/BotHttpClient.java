package backend.academy.scrapper.client;

import backend.academy.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class BotHttpClient {
    private final WebClient webClient;
    private final ScrapperConfigProperties config;

    public BotHttpClient(WebClient.Builder webClientBuilder, ScrapperConfigProperties config) {
        this.config = config;
        webClient = webClientBuilder
                .baseUrl(config.botUrl())
                .filter(logRequest())
                .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
                .filter(logResponse())
                .build();
    }

    public void sendUpdate(LinkUpdate update) {
        webClient
                .post()
                .uri(config.updatesPath())
                .bodyValue(update)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private Mono<ClientResponse> renderApiErrorResponse(ClientResponse clientResponse) {
        return ClientUtils.renderApiErrorResponse(clientResponse, log);
    }

    private ExchangeFilterFunction logRequest() {
        return ClientUtils.logRequest(log);
    }

    private ExchangeFilterFunction logResponse() {
        return ClientUtils.logResponse(log);
    }
}
