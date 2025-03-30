package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.ApiErrorResponse;
import backend.academy.scrapper.model.Subscription;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
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

public class GitHubCheckUpdateClient implements CheckUpdateClient {
    private static final Logger LOG = LoggerFactory.getLogger(GitHubCheckUpdateClient.class);
    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final Pattern GITHUB_URL_REGEX = Pattern.compile("https://github.com/(?<owner>.*)/(?<repo>.*)");

    private final WebClient webClient;

    public GitHubCheckUpdateClient(String gitHubApiUrl) {
        webClient = WebClient.builder()
                .baseUrl(gitHubApiUrl)
                .filter(logRequest())
                .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
                .filter(logResponse())
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
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(ApiErrorResponse.class)
                        .flatMap(error -> Mono.error(new ResponseStatusException(HttpStatus.valueOf(error.code())))))
                .bodyToMono(Response.class)
                .block();

        if (response == null || response.updatedAt() == null) {
            LOG.atInfo()
                    .setMessage("GitHub sent null response or null updated_at")
                    .log();
            return Optional.empty();
        }

        long lastActivityDate = response.updatedAt().toEpochSecond();
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

    private Mono<ClientResponse> renderApiErrorResponse(ClientResponse clientResponse) {
        return ClientUtils.renderApiErrorResponse(clientResponse, LOG);
    }

    private ExchangeFilterFunction logRequest() {
        return ClientUtils.logRequest(LOG);
    }

    private ExchangeFilterFunction logResponse() {
        return ClientUtils.logResponse(LOG);
    }

    private record Response(@JsonProperty("updated_at") ZonedDateTime updatedAt) {}
}
