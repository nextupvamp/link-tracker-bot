package backend.academy.scrapper.controller;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "resilience4j.ratelimiter.instances.default.limit-refresh-period=9999s",
            "resilience4j.ratelimiter.instances.default.limit-for-period=1"
        })
@Import(TestcontainersConfiguration.class)
public class ScrapperControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testRateLimiter() {
        ResponseEntity<String> response =
                restTemplate.postForEntity("http://localhost:" + port + "/tg-chat/1", null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = restTemplate.postForEntity("http://localhost:" + port + "/tg-chat/1", null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
