package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertThrows;

import backend.academy.scrapper.dto.LinkUpdate;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
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
        LinkUpdate linkUpdate = new LinkUpdate("url", "topic", "username", 123L, "preview", null);

        stubFor(post(urlEqualTo(config.updatesPath()))
                .willReturn(aResponse().withFixedDelay(2_000).withStatus(200)));

        for (int i = 0; i != 2; ++i) {
            botHttpClient.sendUpdate(linkUpdate);
        }
        assertThrows(CallNotPermittedException.class, () -> botHttpClient.sendUpdate(linkUpdate));
    }
}
