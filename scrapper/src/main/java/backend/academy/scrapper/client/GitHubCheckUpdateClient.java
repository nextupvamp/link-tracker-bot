package backend.academy.scrapper.client;

import backend.academy.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.model.Subscription;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
public class GitHubCheckUpdateClient implements CheckUpdateClient {
    private static final Pattern GITHUB_URL_REGEX = Pattern.compile("https://github.com/(?<owner>.*)/(?<repo>.*)");

    private final WebClient webClient;
    private final ScrapperConfigProperties config;

    public GitHubCheckUpdateClient(WebClient.Builder webClientBuilder, ScrapperConfigProperties config) {
        this.config = config;
        webClient = webClientBuilder
                .baseUrl(config.gitHubApiUrl())
                .filter(logRequest())
                .filter(ExchangeFilterFunction.ofResponseProcessor(this::renderApiErrorResponse))
                .filter(logResponse())
                .build();
    }

    @Override
    public Optional<Update> checkUpdates(Subscription subscription) {
        Issue[] issues = getIssues(subscription);

        if (issues == null || issues[0] == null) {
            log.atInfo().setMessage("GitHub sent null response").log();
            return Optional.empty();
        }

        Issue issue = issues[0];

        long lastActivityDate = issue.createdAt().toEpochSecond();
        if (lastActivityDate > subscription.lastUpdate()) {
            subscription.lastUpdate(lastActivityDate);
            return Optional.of(Update.builder()
                    .preview(issue.body().substring(0, config.previewSize()))
                    .topic(issue.title())
                    .time(lastActivityDate)
                    .username(issue.user().login())
                    .subscription(subscription)
                    .build());
        }

        return Optional.empty();
    }

    private Issue[] getIssues(Subscription subscription) {
        return webClient
            .get()
            .uri(getRepoIssuePath(subscription.url()))
            .retrieve()
            .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(ApiErrorResponse.class)
                .flatMap(error -> Mono.error(new ResponseStatusException(HttpStatus.valueOf(error.code())))))
            .bodyToMono(Issue[].class)
            .block();
    }

    private String getRepoIssuePath(String repoUrl) {
        Matcher matcher = GITHUB_URL_REGEX.matcher(repoUrl);
        String ownerRepoApiPath = "";
        if (matcher.matches()) {
            ownerRepoApiPath = matcher.group("owner") + "/" + matcher.group("repo");
        }

        return String.format(config.githubRepoIssueFormat(), ownerRepoApiPath);
    }

    private Mono<ClientResponse> renderApiErrorResponse(ClientResponse clientResponse) {
        return ClientUtils.renderApiErrorResponse(clientResponse, log);
    }

    private ExchangeFilterFunction logRequest() {
        return ClientUtils.logRequest(log);
    }

    private ExchangeFilterFunction logResponse() {
        return ClientUtils.logResponse(log);
    }

    private record Issue(String title, String body, @JsonProperty("created_at") ZonedDateTime createdAt, User user) {}

    private record User(String login) {}
}
