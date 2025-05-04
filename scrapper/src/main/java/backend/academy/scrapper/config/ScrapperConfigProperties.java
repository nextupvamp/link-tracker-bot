package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record ScrapperConfigProperties(
        @NotEmpty String githubToken,
        StackOverflowCredentials stackOverflow,
        @NotEmpty String botUrl,
        @NotEmpty String gitHubApiUrl,
        @NotEmpty String stackExchangeApiUrl,
        @Positive int pageSize,
        @NotEmpty String accessType,
        @NotEmpty String githubRepoIssueFormat,
        @Positive int previewSize,
        @NotEmpty String updatesPath) {
    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}
}
