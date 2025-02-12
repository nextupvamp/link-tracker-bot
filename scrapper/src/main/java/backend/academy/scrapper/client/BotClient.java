package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.ApiErrorResponse;
import backend.academy.scrapper.service.LinkUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
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
        if (clientResponse.statusCode().isError()) {
            LOG.atInfo().addKeyValue("api_error_response", clientResponse.statusCode()).log();
            return clientResponse.bodyToMono(ApiErrorResponse.class)
                .flatMap(_ -> Mono.error(new ResponseStatusException(
                    clientResponse.statusCode()
                )));
        }
        return Mono.just(clientResponse);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            var log = LOG.atInfo().addKeyValue("request_method", clientRequest.method());
            log.addKeyValue("request_url", clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                values.forEach(value -> log.addKeyValue(name, value))
            );
            log.log();
            return Mono.just(clientRequest);
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            var log = LOG.atInfo().addKeyValue("response_status", clientResponse.statusCode());
            clientResponse.headers().asHttpHeaders().forEach((name, values) ->
                values.forEach(value -> log.addKeyValue(name, value))
            );
            log.log();
            return Mono.just(clientResponse);
        });
    }
}
