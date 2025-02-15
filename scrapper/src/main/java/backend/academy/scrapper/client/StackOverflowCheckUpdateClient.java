package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.ApiErrorResponse;
import backend.academy.scrapper.model.Subscription;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class StackOverflowCheckUpdateClient implements CheckUpdateClient {
    private static final Logger LOG = LoggerFactory.getLogger(StackOverflowCheckUpdateClient.class);
    private static final Pattern STACK_OVERFLOW_URL_REGEX =
            Pattern.compile("https://stackoverflow\\.com/questions/(?<id>[0-9]+)/.*");
    private static final String STACK_EXCHANGE_API_URL = "https://api.stackexchange.com/2.2";

    private final WebClient webClient;

    public StackOverflowCheckUpdateClient(String stackExchangeApiUrl) {
        webClient = WebClient.builder()
                .baseUrl(stackExchangeApiUrl)
                .filter(logRequest())
                .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
                .filter(logResponse())
                .build();
    }

    // required by specification
    public StackOverflowCheckUpdateClient() {
        this(STACK_EXCHANGE_API_URL);
    }

    @Override
    public Optional<Update> checkUpdates(Subscription subscription) {
        Response response = webClient
                .get()
                .uri(getQuestionApiPath(subscription.url()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(ApiErrorResponse.class)
                        .flatMap(error -> Mono.error(new ResponseStatusException(HttpStatus.valueOf(error.code())))))
                .bodyToMono(Response.class)
                .block();

        if (response == null || response.items() == null) {
            LOG.atInfo().setMessage("StackOverflow sent null response").log();
            return Optional.empty();
        }

        long lastActivityDate = response.items()[0].lastActivityDate();
        if (lastActivityDate > subscription.lastUpdate()) {
            subscription.lastUpdate(lastActivityDate);
            return Optional.of(new Update(subscription, "new update"));
        }

        return Optional.empty();
    }

    private String getQuestionApiPath(String questionUrl) {
        Matcher matcher = STACK_OVERFLOW_URL_REGEX.matcher(questionUrl);

        // this check is necessary for matcher (it doesn't recognize group w/o check),
        // but not for me
        String questionId = "";
        if (matcher.matches()) {
            questionId = matcher.group("id");
        }

        return "/questions/" + questionId + "?site=stackoverflow";
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

    private record Response(Item[] items) {}

    private record Item(@JsonProperty("last_activity_date") long lastActivityDate) {}
}
