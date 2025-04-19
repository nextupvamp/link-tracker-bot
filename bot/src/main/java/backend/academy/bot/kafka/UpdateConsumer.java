package backend.academy.bot.kafka;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.UpdateSender;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "kafka")
@AllArgsConstructor
public class UpdateConsumer {
    private final UpdateSender updateSender;

    @KafkaListener(
            topicPartitions = {
                @TopicPartition(
                        topic = "${app.kafka.topic}",
                        partitions = {"${app.kafka.partitions}"})
            })
    @RetryableTopic(
            attempts = "${app.kafka.attempts}",
            backoff = @Backoff(delay = 1000, multiplier = 2),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            kafkaTemplate = "kafkaTemplate")
    public void consume(ConsumerRecord<String, LinkUpdate> record, Acknowledgment ack) {
        log.atInfo().addKeyValue(record.topic(), record.value()).log();

        updateSender.sendUpdates(record.value());

        ack.acknowledge();
    }
}
