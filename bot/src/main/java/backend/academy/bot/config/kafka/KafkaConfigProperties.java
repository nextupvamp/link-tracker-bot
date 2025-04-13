package backend.academy.bot.config.kafka;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka")
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "kafka")
public record KafkaConfigProperties(@Positive int concurrency, @NotEmpty String groupId) {}
