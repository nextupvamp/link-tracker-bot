package backend.academy.scrapper;

import backend.academy.scrapper.client.BotHttpClient;
import backend.academy.scrapper.client.GitHubCheckUpdateClient;
import backend.academy.scrapper.client.StackOverflowCheckUpdateClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientsConfig {
    @Bean
    public BotHttpClient botClient(WebClient.Builder builder, ScrapperConfigProperties scrapperConfig) {
        return new BotHttpClient(builder, scrapperConfig);
    }

    @Bean
    public GitHubCheckUpdateClient gitHubClient(WebClient.Builder builder, ScrapperConfigProperties scrapperConfig) {
        return new GitHubCheckUpdateClient(builder, scrapperConfig);
    }

    @Bean
    public StackOverflowCheckUpdateClient stackOverflowClient(
            WebClient.Builder builder, ScrapperConfigProperties scrapperConfig) {
        return new StackOverflowCheckUpdateClient(builder, scrapperConfig);
    }
}
