package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class GitHubCheckUpdateClientTest extends CommonClientTest {
    // basically, any of 4xx and 5xx statuses will cause a ResponseStatusException with appropriate
    // status code in it
    @Test
    public void test404ResponseStatus() {
        String repoUrl = "https://github.com/user/notfoundrepo";

        WIRE_MOCK_SERVER.stubFor(get(urlEqualTo("/repos/user/notfoundrepo/issues?per_page=1"))
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
