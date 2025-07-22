package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.scrapper.dto.LinkUpdate;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.util.ArrayList;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BotHttpClientTest extends CommonClientTest {

    // basically, any of 4xx and 5xx statuses will cause a ResponseStatusException with appropriate
    // status code in it
    @Test
    public void test400ResponseStatus() {
        WIRE_MOCK_SERVER.stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\": 400, \"message\": \"Bad Request\"}")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> botHttpClient.sendUpdate(new LinkUpdate("", "", "", 0, "", new ArrayList<>())));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {408, 429, 502, 503, 504})
    @SneakyThrows
    public void testRetry(int responseCode) {
        LinkUpdate linkUpdate = new LinkUpdate("url", "topic", "username", 123L, "preview", null);

        stubFor(post(urlEqualTo(config.updatesPath()))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withStatus(responseCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\": " + responseCode + ", \"message\": \"Error\"}"))
                .willSetStateTo("retry"));

        stubFor(post(urlEqualTo(config.updatesPath()))
                .inScenario("retry")
                .whenScenarioStateIs("retry")
                .willReturn(aResponse().withStatus(200)));

        botHttpClient.sendUpdate(linkUpdate);
    }
}
