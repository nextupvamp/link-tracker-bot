package backend.academy.bot.service;

import backend.academy.bot.model.Link;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

public class ScrapperClient {
    private static final String TG_CHAT_PATH = "/tg-chat/";
    private static final String LINKS_PATH = "/links?Tg-Chat-Id=";
    private final WebClient webClient;

    public ScrapperClient(String scrapperUrl) {
        webClient = WebClient.create(scrapperUrl);
        webClient = WebClient.builder()
            .baseUrl(scrapperUrl)
            .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
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
            .map(Optional::of)
            .onErrorReturn(Optional.empty())
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
}
