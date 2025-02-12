package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.ApiErrorResponse;
import backend.academy.scrapper.model.Subscription;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

public class StackOverflowClient implements Client {
    // it's used to extract question id from url
public class StackOverflowCheckUpdateClient implements CheckUpdateClient {
    private static final Pattern STACK_OVERFLOW_URL_REGEX =
        Pattern.compile("https://stackoverflow\\.com/questions/(?<id>[0-9]+)/.*");
    private static final String STACK_EXCHANGE_API_URL = "https://api.stackexchange.com/2.2";

    private final WebClient webClient;

    public StackOverflowClient(String stackExchangeApiUrl) {
        webClient = WebClient.create(stackExchangeApiUrl);
    public StackOverflowCheckUpdateClient(String stackExchangeApiUrl) {
        webClient = WebClient.builder()
            .baseUrl(stackExchangeApiUrl)
            .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
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
            .onStatus(HttpStatusCode::isError, resp -> resp
                .bodyToMono(ApiErrorResponse.class)
                .flatMap(error -> Mono.error(
                    new ResponseStatusException(HttpStatus.valueOf(error.code())))
                )
            )
            .bodyToMono(Response.class)
            .block();
        // add 4xx, 5xx status check

        if (response == null || response.items() == null) {
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

    // those are used to extract exact field from an api response
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
    private record Response(Item[] items) {
    }

    private record Item(@JsonProperty("last_activity_date") long lastActivityDate) {
    }
}
