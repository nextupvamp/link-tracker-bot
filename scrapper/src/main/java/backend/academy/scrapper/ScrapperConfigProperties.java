package backend.academy.scrapper;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record ScrapperConfigProperties(
        @NotEmpty String githubToken,
        StackOverflowCredentials stackOverflow,
        String botUrl,
        String gitHubApiUrl,
        String stackExchangeApiUrl,
        int pageSize,
        String accessType,
        String githubRepoIssueFormat,
        int previewSize,
        String updatesPath) {
    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}
}
