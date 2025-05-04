package backend.academy.bot.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertThrows;

import backend.academy.bot.dto.LinkSet;
import backend.academy.bot.model.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {
            "app.resilience.circuit-breaker-minimum-number-of-calls=2",
            "app.resilience.circuit-breaker-sliding-window-size=9999",
            "app.resilience.circuit-breaker-slow-call-duration-threshold=1"
        })
public class CircuitBreakerTest extends CommonClientTest {
    @Test
    @SneakyThrows
    public void testCircuitBreaker() {
        long chatId = 0L;
        LinkSet linkSet = new LinkSet(Set.of(new Link("url")), 1);

        stubFor(get(urlEqualTo(config.linkPath() + chatId))
                .willReturn(aResponse()
                        .withFixedDelay(2_000)
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(new ObjectMapper().writeValueAsString(linkSet))));

        for (int i = 0; i != 2; ++i) {
            scrapperClient.getAllLinks(chatId);
        }
        assertThrows(CallNotPermittedException.class, () -> scrapperClient.getAllLinks(chatId));
    }
}
