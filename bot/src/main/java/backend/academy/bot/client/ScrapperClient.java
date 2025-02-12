package backend.academy.bot.client;

import backend.academy.bot.exception.ApiErrorResponse;
import backend.academy.bot.model.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class ScrapperClient {
    private static final Logger LOG = LoggerFactory.getLogger(ScrapperClient.class);

    private static final String TG_CHAT_PATH = "/tg-chat/";
    private static final String LINKS_PATH = "/links?Tg-Chat-Id=";

    private final WebClient webClient;

    public ScrapperClient(String scrapperUrl) {
        webClient = WebClient.builder()
            .baseUrl(scrapperUrl)
            .filter(logRequest())
            .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
            .filter(logResponse())
            .build();
    }

    public void addChat(long chatId) {
        webClient
            .post()
            .uri(TG_CHAT_PATH + chatId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> response
                .bodyToMono(ApiErrorResponse.class)
                .flatMap(error -> Mono.error(
                    new ResponseStatusException(HttpStatus.valueOf(error.code())))
                )
            )
            .bodyToMono(Link.class)
            .block();
    }

    // not required by the specification but required to say that this client
    // can use all the functionality of the scrapper service
    public void deleteChat(long chatId) {
        webClient.delete()
            .uri(TG_CHAT_PATH + chatId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> response
                .bodyToMono(ApiErrorResponse.class)
                .flatMap(error -> Mono.error(
                    new ResponseStatusException(HttpStatus.valueOf(error.code())))
                )
            )
            .bodyToMono(Link.class)
            .block();
    }

    public LinkSet getAllLinks(long chatId) {
        return webClient
            .get()
            .uri(LINKS_PATH + chatId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> response
                .bodyToMono(ApiErrorResponse.class)
                .flatMap(error -> Mono.error(
                    new ResponseStatusException(HttpStatus.valueOf(error.code())))
                )
            )
            .bodyToMono(LinkSet.class)
            .block();
    }

    public void addLink(long chatId, Link link) {
        webClient
            .post()
            .uri(LINKS_PATH + chatId)
            .bodyValue(link)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> response
                .bodyToMono(ApiErrorResponse.class)
                .flatMap(error -> Mono.error(
                    new ResponseStatusException(HttpStatus.valueOf(error.code())))
                )
            )
            .bodyToMono(Link.class)
            .block();
    }

    public Link removeLink(long chatId, Link link) {
        return webClient
            .method(HttpMethod.DELETE)
            .uri(LINKS_PATH + chatId)
            .bodyValue(link)
            .retrieve()
            .onStatus(HttpStatusCode::isError, response -> response
                .bodyToMono(ApiErrorResponse.class)
                .flatMap(error -> Mono.error(
                    new ResponseStatusException(HttpStatus.valueOf(error.code())))
                )
            )
            .bodyToMono(Link.class)
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
