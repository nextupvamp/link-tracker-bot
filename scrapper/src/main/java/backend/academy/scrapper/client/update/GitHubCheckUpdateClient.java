package backend.academy.scrapper.client.update;

import backend.academy.scrapper.client.util.ClientUtils;
import backend.academy.scrapper.config.resilience.ResilienceConfig;
import backend.academy.scrapper.config.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class GitHubCheckUpdateClient implements CheckUpdateClient {

    private static final Pattern GITHUB_URL_REGEX = Pattern.compile("https://github.com/(?<owner>.*)/(?<repo>.*)");

    private final WebClient webClient;
    private final ScrapperConfigProperties config;
    private final ResilienceConfig.ResilienceFeatures resilienceFeatures;

    public GitHubCheckUpdateClient(
            WebClient.Builder webClientBuilder,
            ScrapperConfigProperties config,
            ResilienceConfig.ResilienceFeatures resilienceFeatures) {
        this.config = config;
        webClient = webClientBuilder
                .baseUrl(config.gitHubApiUrl())
                .filter(logRequest())
                .filter(logResponse())
                .build();
        this.resilienceFeatures = resilienceFeatures;
    }

    @Override
    public Optional<Update> checkUpdates(Subscription subscription) {
        if (subscription.site() != Site.GITHUB) {
            return Optional.empty();
        }

        Issue[] issues = getIssues(subscription);

        if (issues == null || issues[0] == null) {
            log.atWarn().setMessage("GitHub sent null response").log();
            return Optional.empty();
        }

        Issue issue = issues[0];
        long lastActivityDate = issue.createdAt().toEpochSecond();
        if (lastActivityDate > subscription.lastUpdate()) {
            subscription.lastUpdate(lastActivityDate);
            return Optional.of(Update.builder()
                    .preview(issue.body())
                    .topic(issue.title())
                    .time(lastActivityDate)
                    .username(issue.user().login())
                    .subscription(subscription)
                    .build());
        }

        return Optional.empty();
    }

    private Issue[] getIssues(Subscription subscription) {
        return ClientUtils.applyResilienceFeatures(
                        webClient
                                .get()
                                .uri(getRepoIssuePath(subscription.url()))
                                .retrieve()
                                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(ApiErrorResponse.class)
                                        .flatMap(ClientUtils::renderError))
                                .bodyToMono(Issue[].class),
                        resilienceFeatures)
                .block();
    }

    private String getRepoIssuePath(String repoUrl) {
        Matcher matcher = GITHUB_URL_REGEX.matcher(repoUrl);
        String ownerRepoApiPath = "";
        if (matcher.matches()) {
            ownerRepoApiPath = String.format("%s/%s", matcher.group("owner"), matcher.group("repo"));
        }

        return String.format(config.githubRepoIssueFormat(), ownerRepoApiPath);
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
