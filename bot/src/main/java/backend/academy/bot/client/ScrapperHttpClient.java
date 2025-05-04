package backend.academy.bot.client;

import backend.academy.bot.config.bot.BotConfigProperties;
import backend.academy.bot.config.resilience.ResilienceConfig;
import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.dto.LinkSet;
import backend.academy.bot.exception.RetryableException;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.Link;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ScrapperHttpClient implements ScrapperClient {
    private final ResilienceConfig.ResilienceFeatures resilienceFeatures;
    private final WebClient webClient;
    private final String tgChatPath;
    private final String linksPath;

    public ScrapperHttpClient(
            WebClient.Builder webClientBuilder,
            BotConfigProperties properties,
            ResilienceConfig.ResilienceFeatures resilienceFeatures) {
        tgChatPath = properties.tgChatPath();
        linksPath = properties.linkPath();
        webClient = webClientBuilder
                .baseUrl(properties.scrapperUrl())
                .filter(logRequest())
                .filter(logResponse())
                .build();
        this.resilienceFeatures = resilienceFeatures;
    }

    @Override
    public ChatData getChatData(long chatId) {
        return execRequest(webClient.get(), tgChatPath + chatId, ChatData.class);
    }

    @Override
    public void updateChat(ChatData chatData) {
        execRequestWithBody(webClient.patch(), tgChatPath, Object.class, chatData);
    }

    @Override
    public void addChat(long chatId) {
        execRequest(webClient.post(), tgChatPath + chatId, Object.class);
    }

    @Override
    public void deleteChat(long chatId) {
        execRequest(webClient.delete(), tgChatPath + chatId, Object.class);
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
        return applyResilienceFeatures(request.uri(uri)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(ApiErrorResponse.class)
                                .flatMap(this::renderError))
                        .bodyToMono(clazz))
                .block();
    }

    private <T, R> R execRequestWithBody(WebClient.RequestBodyUriSpec request, String uri, Class<R> clazz, T body) {
        return applyResilienceFeatures(request.uri(uri)
                        .bodyValue(body)
                        .retrieve()
                        .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(ApiErrorResponse.class)
                                .flatMap(this::renderError))
                        .bodyToMono(clazz))
                .block();
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

    private <T> Mono<T> renderError(ApiErrorResponse error) {
        int code = error.code();
        if (code == 408 || code == 429 || code == 502 || code == 503 || code == 504) {
            return Mono.error(new RetryableException(HttpStatus.valueOf(code)));
        }
        return Mono.error(new ResponseStatusException(HttpStatus.valueOf(code)));
    }

    private <T> Mono<T> applyResilienceFeatures(Mono<T> mono) {
        return mono.transformDeferred(CircuitBreakerOperator.of(resilienceFeatures.circuitBreaker()))
                .transformDeferred(RetryOperator.of(resilienceFeatures.retry()))
                .transformDeferred(TimeLimiterOperator.of(resilienceFeatures.timeLimiter()));
    }
}
