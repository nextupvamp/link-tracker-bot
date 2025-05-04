package backend.academy.bot;

import backend.academy.bot.config.bot.BotConfigProperties;
import backend.academy.bot.config.cache.CacheConfigProperties;
import backend.academy.bot.config.kafka.KafkaConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({BotConfigProperties.class, KafkaConfigProperties.class, CacheConfigProperties.class})
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
