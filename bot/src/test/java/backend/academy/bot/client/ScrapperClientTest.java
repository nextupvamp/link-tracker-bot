package backend.academy.bot.client;

import backend.academy.bot.BotConfigProperties;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScrapperClientTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(1487);

    private final BotConfigProperties config = new BotConfigProperties(
        null,
        "http://localhost:1487",
        null,
        "/tg-chat/",
        "/links?Tg-Chat-Id="
    );

    private final ScrapperClient scrapperClient = new ScrapperClient(WebClient.builder(), config);

    // basically, any of 4xx and 5xx statuses will cause a ResponseStatusException with appropriate
    // status code in it
    @Test
    public void test400ResponseStatus() {
        long chatId = 0;

        stubFor(post(urlEqualTo("/tg-chat/" + chatId))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\": 400, \"message\": \"Bad Request\"}")));

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> scrapperClient.addChat(chatId));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void test404ResponseStatus() {
        long chatId = 0;

        stubFor(delete(urlEqualTo("/tg-chat/" + chatId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\": 404, \"message\": \"Not Found\"}")));

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> scrapperClient.deleteChat(chatId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
