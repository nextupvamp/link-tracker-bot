package backend.academy.bot.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.bot.dto.LinkSet;
import backend.academy.bot.model.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ScrapperHttpClientTest extends CommonClientTest {

    // basically, any of 4xx and 5xx statuses will cause a ResponseStatusException with appropriate
    // status code in it
    @Test
    public void test400ResponseStatus() {
        long chatId = 0L;

        WIRE_MOCK_SERVER.stubFor(post(urlEqualTo(config.tgChatPath() + chatId))
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
        long chatId = 0L;

        WIRE_MOCK_SERVER.stubFor(delete(urlEqualTo(config.tgChatPath() + chatId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\": 404, \"message\": \"Not Found\"}")));

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> scrapperClient.deleteChat(chatId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {408, 429, 502, 503, 504})
    @SneakyThrows
    public void testRetry(int responseCode) {
        long chatId = 0L;
        LinkSet linkSet = new LinkSet(Set.of(new Link("url")), 1);

        stubFor(get(urlEqualTo(config.linkPath() + chatId))
                .inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse()
                        .withStatus(responseCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"code\": " + responseCode + ", \"message\": \"Error\"}"))
                .willSetStateTo("retry"));

        stubFor(get(urlEqualTo(config.linkPath() + chatId))
                .inScenario("retry")
                .whenScenarioStateIs("retry")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(new ObjectMapper().writeValueAsString(linkSet))));

        assertThat(linkSet).isEqualTo(scrapperClient.getAllLinks(chatId));
    }
}
