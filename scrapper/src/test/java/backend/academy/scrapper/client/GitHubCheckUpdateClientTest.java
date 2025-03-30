package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GitHubCheckUpdateClientTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    private final GitHubCheckUpdateClient gitHubCheckUpdateClient =
            new GitHubCheckUpdateClient("http://localhost:8080");

    // basically, any of 4xx and 5xx statuses will cause a ResponseStatusException with appropriate
    // status code in it
    @Test
    public void test404ResponseStatus() {
        String repoUrl = "https://github.com/user/notfoundrepo";

        stubFor(get(urlEqualTo("/repos/user/notfoundrepo"))
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
