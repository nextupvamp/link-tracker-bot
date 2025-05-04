package backend.academy.bot.client;

import backend.academy.bot.config.bot.BotConfigProperties;
import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.dto.LinkSet;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.Link;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ScrapperHttpClient implements ScrapperClient {
    private final WebClient webClient;
    private final String tgChatPath;
    private final String linksPath;

    public ScrapperHttpClient(WebClient.Builder webClientBuilder, BotConfigProperties properties) {
        tgChatPath = properties.tgChatPath();
        linksPath = properties.linkPath();
        webClient = webClientBuilder
                .baseUrl(properties.scrapperUrl())
                .filter(logRequest())
                .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
                .filter(logResponse())
                .build();
    }

    @Override
    public ChatData getChatData(long chatId) {
        return execRequest(webClient.get(), tgChatPath + "/" + chatId, ChatData.class);
    }

    @Override
    public void updateChat(ChatData chatData) {
        execRequestWithBody(webClient.patch(), tgChatPath, Object.class, chatData);
    }

    @Override
    public void addChat(long chatId) {
        execRequest(webClient.post(), tgChatPath + "/" + chatId, Object.class);
    }

    @Override
    public void deleteChat(long chatId) {
        execRequest(webClient.delete(), tgChatPath + "/" + chatId, Object.class);
    }

    @Override
    public LinkSet getAllLinks(long chatId) {
        return execRequest(webClient.get(), linksPath + chatId, LinkSet.class);
    }

    @Override
    public void addLink(long chatId, Link link) {
        execRequestWithBody(webClient.post(), linksPath + chatId, Object.class, link);
    }

    @Override
    public void removeLink(long chatId, Link link) {
        execRequestWithBody(webClient.method(HttpMethod.DELETE), linksPath + chatId, Object.class, link);
    }

    private <R> R execRequest(WebClient.RequestHeadersUriSpec<?> request, String uri, Class<R> clazz) {
        return request.uri(uri)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(ApiErrorResponse.class)
                        .flatMap(error -> Mono.error(new ResponseStatusException(HttpStatus.valueOf(error.code())))))
                .bodyToMono(clazz)
                .block();
    }

    private <T, R> R execRequestWithBody(WebClient.RequestBodyUriSpec request, String uri, Class<R> clazz, T body) {
        return request.uri(uri)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(ApiErrorResponse.class)
                        .flatMap(error -> Mono.error(new ResponseStatusException(HttpStatus.valueOf(error.code())))))
                .bodyToMono(clazz)
                .block();
    }

    private Mono<ClientResponse> renderApiErrorResponse(ClientResponse clientResponse) {
        if (clientResponse.statusCode().isError()) {
            log.atInfo()
                    .addKeyValue("api_error_response", clientResponse.statusCode())
                    .log();
            return clientResponse
                    .bodyToMono(ApiErrorResponse.class)
                    .flatMap(ignored -> Mono.error(new ResponseStatusException(clientResponse.statusCode())));
        }
        return Mono.just(clientResponse);
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            var log = ScrapperHttpClient.log
                    .atInfo()
                    .addKeyValue("request_method", clientRequest.method())
                    .addKeyValue("request_url", clientRequest.url());
            var headers = clientRequest.headers().asSingleValueMap();
            for (var entry : headers.entrySet()) {
                log = log.addKeyValue(entry.getKey(), entry.getValue());
            }
            log.log();
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            var log = ScrapperHttpClient.log.atInfo().addKeyValue("response_status", clientResponse.statusCode());
            var headers = clientResponse.headers().asHttpHeaders().asSingleValueMap();
            for (var entry : headers.entrySet()) {
                log = log.addKeyValue(entry.getKey(), entry.getValue());
            }
            log.log();
            return Mono.just(clientResponse);
        });
    }
}
