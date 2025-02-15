package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.ApiErrorResponse;
import org.slf4j.Logger;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class ClientUtils {
    public static ExchangeFilterFunction logRequest(Logger logger) {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            var log = logger.atInfo()
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
            var log = logger.atInfo().addKeyValue("response_status", clientResponse.statusCode());
            var headers = clientResponse.headers().asHttpHeaders().asSingleValueMap();
            for (var entry : headers.entrySet()) {
                log = log.addKeyValue(entry.getKey(), entry.getValue());
            }
            log.log();
            return Mono.just(clientResponse);
        });
    }

    public static Mono<ClientResponse> renderApiErrorResponse(ClientResponse clientResponse, Logger logger) {
        if (clientResponse.statusCode().isError()) {
            logger.atInfo()
                    .addKeyValue("api_error_response", clientResponse.statusCode())
                    .log();
            return clientResponse
                    .bodyToMono(ApiErrorResponse.class)
                    .flatMap(ignored -> Mono.error(new ResponseStatusException(clientResponse.statusCode())));
        }
        return Mono.just(clientResponse);
    }
}
