package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.scrapper.client.update.GitHubCheckUpdateClient;
import backend.academy.scrapper.config.ScrapperConfigProperties;
import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

public class GitHubCheckUpdateClientTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(1487);

    private final ScrapperConfigProperties config = new ScrapperConfigProperties(
            null, null, null, "http://localhost:1487", null, 229, null, "repos/%s/issues?per_page=1", 229, "/updates");

    private final GitHubCheckUpdateClient gitHubCheckUpdateClient =
            new GitHubCheckUpdateClient(WebClient.builder(), config);

    // basically, any of 4xx and 5xx statuses will cause a ResponseStatusException with appropriate
    // status code in it
    @Test
    public void test404ResponseStatus() {
        String repoUrl = "https://github.com/user/notfoundrepo";

        stubFor(get(urlEqualTo("/repos/user/notfoundrepo/issues?per_page=1"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\": 404, \"message\": \"Not Found\"}")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> gitHubCheckUpdateClient.checkUpdates(new Subscription(repoUrl, Site.GITHUB)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
