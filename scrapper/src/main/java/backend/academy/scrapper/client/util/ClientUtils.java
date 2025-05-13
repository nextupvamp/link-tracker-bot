package backend.academy.scrapper.client.util;

import backend.academy.scrapper.config.resilience.ResilienceConfig;
import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.exception.RetryableException;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import java.util.Set;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@UtilityClass
public class ClientUtils {
    private static final Set<Integer> RETRYABLE_CODES = Set.of(408, 429, 502, 503, 504);

    public static ExchangeFilterFunction logRequest(Logger logger) {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            var log = logger.atDebug()
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

    public static ExchangeFilterFunction logResponse(Logger logger) {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            var log = logger.atDebug().addKeyValue("response_status", clientResponse.statusCode());
            var headers = clientResponse.headers().asHttpHeaders().asSingleValueMap();
            for (var entry : headers.entrySet()) {
                log = log.addKeyValue(entry.getKey(), entry.getValue());
            }
            log.log();
            return Mono.just(clientResponse);
        });
    }

    public static <T> Mono<T> renderError(ApiErrorResponse error) {
        int code = error.code();
        if (RETRYABLE_CODES.contains(code)) {
            return Mono.error(new RetryableException(HttpStatus.valueOf(code)));
        }
        return Mono.error(new ResponseStatusException(HttpStatus.valueOf(code)));
    }

    public static <T> Mono<T> applyResilienceFeatures(
            Mono<T> mono, ResilienceConfig.ResilienceFeatures resilienceFeatures) {
        return mono.transformDeferred(CircuitBreakerOperator.of(resilienceFeatures.circuitBreaker()))
                .transformDeferred(RetryOperator.of(resilienceFeatures.retry()))
                .transformDeferred(TimeLimiterOperator.of(resilienceFeatures.timeLimiter()));
    }
}
