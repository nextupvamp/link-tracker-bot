package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.dto.LinkUpdate;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.HashMap;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

public class BotHttpClientTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(1489);

    private final ScrapperConfigProperties config = new ScrapperConfigProperties(
            null, null, "http://localhost:1489", null, null, 229, null, null, 229, "/updates");

    private final BotHttpClient botHttpClient = new BotHttpClient(WebClient.builder(), config);

    // basically, any of 4xx and 5xx statuses will cause a ResponseStatusException with appropriate
    // status code in it
    @Test
    public void test400ResponseStatus() {
        stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\": 400, \"message\": \"Bad Request\"}")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> botHttpClient.sendUpdate(new LinkUpdate("", "", "", 0, "", new HashMap<>())));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
