package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.ApiErrorResponse;
import backend.academy.scrapper.model.Subscription;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
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

public class GitHubClient implements Client {
    // comments are the same as in the stackoverflow implementation
public class GitHubCheckUpdateClient implements CheckUpdateClient {
    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final Pattern GITHUB_URL_REGEX =
        Pattern.compile("https://github.com/(?<owner>.*)/(?<repo>.*)");

    private final WebClient webClient;

    public GitHubClient(String gitHubApiUrl) {
        webClient = WebClient.create(gitHubApiUrl);
    public GitHubCheckUpdateClient(String gitHubApiUrl) {
        webClient = WebClient.builder()
            .baseUrl(gitHubApiUrl)
            .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
            .build();
    }

    // required by specification
    public GitHubCheckUpdateClient() {
        this(GITHUB_API_URL);
    }

    @Override
    public Optional<Update> checkUpdates(Subscription subscription) {
        Response response = webClient
            .get()
            .uri(getOwnerRepoApiPath(subscription.url()))
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

        long lastActivityDate = response.updated_at().toEpochSecond();
        if (response == null || response.updatedAt() == null) {
            return Optional.empty();
        }
        if (lastActivityDate > subscription.lastUpdate()) {
            subscription.lastUpdate(lastActivityDate);
            // description will appear at the next iterations,
            // but now it's unused
            return Optional.of(new Update(subscription, ""));
        }

        return Optional.empty();
    }

    private String getOwnerRepoApiPath(String repoUrl) {
        Matcher matcher = GITHUB_URL_REGEX.matcher(repoUrl);
        String ownerRepoApiPath = "";
        if (matcher.matches()) {
            ownerRepoApiPath = matcher.group("owner") + "/" + matcher.group("repo");
        }

        return "repos/" + ownerRepoApiPath;
    }

    private record Response(ZonedDateTime updated_at) {
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
}
