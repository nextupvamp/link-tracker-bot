package backend.academy.bot.controller;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.bot.dto.LinkUpdate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "resilience4j.ratelimiter.instances.default.limit-refresh-period=9999s",
            "resilience4j.ratelimiter.instances.default.limit-for-period=1"
        })
public class UpdateControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testRateLimiter() {
        LinkUpdate linkUpdate = new LinkUpdate("url", "topic", "username", 123L, "preview", null);

        ResponseEntity<String> response =
                restTemplate.postForEntity("http://localhost:" + port + "/updates", linkUpdate, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = restTemplate.postForEntity("http://localhost:" + port + "/updates", linkUpdate, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
