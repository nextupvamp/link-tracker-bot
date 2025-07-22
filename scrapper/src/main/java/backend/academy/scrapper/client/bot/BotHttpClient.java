package backend.academy.scrapper.client.bot;

import backend.academy.scrapper.client.util.ClientUtils;
import backend.academy.scrapper.config.resilience.ResilienceConfig;
import backend.academy.scrapper.config.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.dto.ApiErrorResponse;
import backend.academy.scrapper.dto.LinkUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class BotHttpClient implements BotClient {

    private final WebClient webClient;
    private final ScrapperConfigProperties config;
    private final ResilienceConfig.ResilienceFeatures resilienceFeatures;

    public BotHttpClient(
            WebClient.Builder webClientBuilder,
            ScrapperConfigProperties config,
            ResilienceConfig.ResilienceFeatures resilienceFeatures) {
        this.config = config;
        webClient = webClientBuilder
                .baseUrl(config.botUrl())
                .filter(logRequest())
                .filter(logResponse())
                .build();
        this.resilienceFeatures = resilienceFeatures;
    }

    @Override
    public void sendUpdate(LinkUpdate update) {
        ClientUtils.applyResilienceFeatures(
                        webClient
                                .post()
                                .uri(config.updatesPath())
                                .bodyValue(update)
                                .retrieve()
                                .onStatus(
                                        HttpStatusCode::isError, response -> response.bodyToMono(ApiErrorResponse.class)
                                                .flatMap(ClientUtils::renderError))
                                .bodyToMono(String.class),
                        resilienceFeatures)
                .block();
    }

    private ExchangeFilterFunction logRequest() {
        return ClientUtils.logRequest(log);
    }

    private ExchangeFilterFunction logResponse() {
        return ClientUtils.logResponse(log);
    }
}
