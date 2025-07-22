package backend.academy.scrapper;

import backend.academy.scrapper.config.kafka.KafkaConfigProperties;
import backend.academy.scrapper.config.resilience.ResilienceConfigProperties;
import backend.academy.scrapper.config.scrapper.ScrapperConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    ScrapperConfigProperties.class,
    KafkaConfigProperties.class,
    ResilienceConfigProperties.class
})
@Slf4j
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
