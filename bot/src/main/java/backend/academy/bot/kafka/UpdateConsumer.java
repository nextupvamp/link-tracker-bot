package backend.academy.bot.kafka;

import backend.academy.bot.config.kafka.KafkaConfig;
import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.UpdateSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app", name = "enable-kafka", havingValue = "true")
@AllArgsConstructor
public class UpdateConsumer {
    private final UpdateSender updateSender;

    @KafkaListener(topics = {"${app.kafka.topic}"})
    @RetryableTopic(
            attempts = "${app.kafka.retry.attempts}",
            backoff =
                    @Backoff(
                            delayExpression = "${app.kafka.retry.delay}",
                            multiplierExpression = "${app.kafka.retry.multiplier}"),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            kafkaTemplate = KafkaConfig.KAFKA_TEMPLATE_NAME)
    public void consume(ConsumerRecord<String, LinkUpdate> record, Acknowledgment ack) {
        log.atInfo().addKeyValue(record.topic(), record.value()).log();

        updateSender.sendUpdates(record.value());

        ack.acknowledge();
    }
}
