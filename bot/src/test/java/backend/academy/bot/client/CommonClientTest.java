package backend.academy.bot.client;

import backend.academy.bot.config.bot.BotConfigProperties;
import backend.academy.bot.config.resilience.ResilienceConfig;
import backend.academy.bot.config.resilience.ResilienceConfigProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest(
        classes = {
            ResilienceConfig.class,
            ResilienceConfig.ResilienceFeatures.class,
        })
@EnableConfigurationProperties(ResilienceConfigProperties.class)
public class CommonClientTest {

    protected final BotConfigProperties config =
            new BotConfigProperties(null, "http://localhost:8080", null, "/tg-chat/", "/links?Tg-Chat-Id=");

    protected static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(8080);

    protected ScrapperHttpClient scrapperClient;

    @Autowired
    private ResilienceConfig.ResilienceFeatures resilienceFeatures;

    @BeforeAll
    public static void startServer() {
        WIRE_MOCK_SERVER.start();
    }

    @BeforeEach
    public void setUp() {
        scrapperClient = new ScrapperHttpClient(WebClient.builder(), config, resilienceFeatures);
    }
}
