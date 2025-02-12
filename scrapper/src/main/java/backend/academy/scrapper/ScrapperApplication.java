package backend.academy.scrapper;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GitHubCheckUpdateClient;
import backend.academy.scrapper.client.StackOverflowCheckUpdateClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({ScrapperConfig.class})
@EnableScheduling
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }

    @Bean
    public BotClient botClient(ScrapperConfig scrapperConfig) {
        return new BotClient(scrapperConfig.botUrl());
    }

    @Bean
    public GitHubCheckUpdateClient gitHubClient(ScrapperConfig scrapperConfig) {
        return new GitHubCheckUpdateClient(scrapperConfig.gitHubApiUrl());
    }

    @Bean
    public StackOverflowCheckUpdateClient stackOverflowClient(ScrapperConfig scrapperConfig) {
        return new StackOverflowCheckUpdateClient(scrapperConfig.stackExchangeApiUrl());
    }
}
