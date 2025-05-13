package backend.academy.scrapper.config.kafka;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka")
public record KafkaConfigProperties(
        @NotEmpty String topic, @Positive int partitions, @Positive short replicationFactor) {}
