package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Subscription;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.reactive.function.client.WebClient;

public class GitHubClient implements Client {
    private static final Pattern GITHUB_URL_REGEX =
        Pattern.compile("https://github.com/(?<owner>.*)/(?<repo>.*)");

    private final WebClient webClient;

    public GitHubClient(String githubApiUrl) {
        this.webClient = WebClient.create(githubApiUrl);
    }

    @Override
    public Optional<Update> checkUpdates(Subscription subscription) {
        Response response = webClient
            .get()
            .uri(getOwnerRepoApiPath(subscription.url()))
            .retrieve()
            .bodyToMono(Response.class)
            .block();
        // add 4xx, 5xx status check

        long lastActivityDate = response.updated_at().toEpochSecond();
        if (lastActivityDate > subscription.lastUpdate()) {
            subscription.lastUpdate(lastActivityDate);
            return Optional.of(new Update(subscription.url(), ""));
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
    }
}
