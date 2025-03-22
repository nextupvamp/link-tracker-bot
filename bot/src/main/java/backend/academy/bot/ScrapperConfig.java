package backend.academy.bot;

import backend.academy.bot.client.ScrapperClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

public class ScrapperConfig {
    @Bean
    public ScrapperClient scrapperClient(WebClient.Builder builder, BotConfigProperties botConfigProperties) {
        return new ScrapperClient(builder, botConfigProperties);
    }
}
