package backend.academy.scrapper.client;

import backend.academy.scrapper.service.LinkUpdate;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.ArrayList;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BotClientTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8080);

    private final BotClient botClient = new BotClient("http://localhost:8080");

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
                () -> botClient.sendUpdate(new LinkUpdate(0, "", "", new ArrayList<>())));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
