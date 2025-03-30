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

public class StackOverflowCheckUpdateClientTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    private final StackOverflowCheckUpdateClient stackOverflowCheckUpdateClient =
            new StackOverflowCheckUpdateClient("http://localhost:8080");

    // basically, any of 4xx and 5xx statuses will cause a ResponseStatusException with appropriate
    // status code in it
    @Test
    public void test404ResponseStatus() {
        String questionUrl = "https://stackoverflow.com/questions/0/bububu";

        stubFor(get(urlEqualTo("/questions/0?site=stackoverflow"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\": 404, \"message\": \"Not Found\"}")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> stackOverflowCheckUpdateClient.checkUpdates(new Subscription(questionUrl, Site.STACKOVERFLOW)));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
