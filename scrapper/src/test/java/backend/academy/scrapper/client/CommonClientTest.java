package backend.academy.scrapper.client;

import backend.academy.scrapper.client.bot.BotHttpClient;
import backend.academy.scrapper.client.update.GitHubCheckUpdateClient;
import backend.academy.scrapper.client.update.StackOverflowCheckUpdateClient;
import backend.academy.scrapper.config.resilience.ResilienceConfig;
import backend.academy.scrapper.config.resilience.ResilienceConfigProperties;
import backend.academy.scrapper.config.scrapper.ScrapperConfigProperties;
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
    protected final ScrapperConfigProperties config = new ScrapperConfigProperties(
            null,
            null,
            "http://localhost:8080/",
            "http://localhost:8080/",
            "http://localhost:8080/",
            100,
            null,
            "/repos/%s/issues?per_page=1",
            229,
            "/updates",
            "http");

    protected static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(8080);

    protected BotHttpClient botHttpClient;
    protected GitHubCheckUpdateClient gitHubCheckUpdateClient;
    protected StackOverflowCheckUpdateClient stackOverflowCheckUpdateClient;

    @Autowired
    private ResilienceConfig.ResilienceFeatures resilienceFeatures;

    @BeforeAll
    public static void startServer() {
        WIRE_MOCK_SERVER.start();
    }

    @BeforeEach
    public void setUp() {
        botHttpClient = new BotHttpClient(WebClient.builder(), config, resilienceFeatures);
        gitHubCheckUpdateClient = new GitHubCheckUpdateClient(WebClient.builder(), config, resilienceFeatures);
        stackOverflowCheckUpdateClient =
                new StackOverflowCheckUpdateClient(WebClient.builder(), config, resilienceFeatures);
    }
}
