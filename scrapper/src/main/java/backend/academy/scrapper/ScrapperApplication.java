package backend.academy.scrapper;

import backend.academy.scrapper.service.BotClient;
import backend.academy.scrapper.service.GitHubClient;
import backend.academy.scrapper.service.StackOverflowClient;
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
    public GitHubClient gitHubClient(ScrapperConfig scrapperConfig) {
        return new GitHubClient(scrapperConfig.gitHubApiUrl());
    }

    @Bean
    public StackOverflowClient stackOverflowClient(ScrapperConfig scrapperConfig) {
        return new StackOverflowClient(scrapperConfig.stackExchangeApiUrl());
    }
}
