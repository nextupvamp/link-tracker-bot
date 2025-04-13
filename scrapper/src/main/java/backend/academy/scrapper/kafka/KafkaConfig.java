package backend.academy.scrapper.kafka;

import backend.academy.scrapper.config.KafkaConfigProperties;
import backend.academy.scrapper.dto.LinkUpdate;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "kafka")
@AllArgsConstructor
public class KafkaConfig {
    private final KafkaConfigProperties config;
    private final KafkaProperties kafkaProperties;

    @Bean
    public Admin admin() {
        return Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers()));
    }

    @Bean
    public KafkaTemplate<String, LinkUpdate> kafkaUpdateTemplate() {
        var properties = kafkaProperties.buildProducerProperties();

        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LinkUpdateSerializer.class);
        properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(properties));
    }

    @Bean
    public NewTopic newTopic() {
        return new NewTopic(config.topic(), config.partitions(), config.replicationFactor());
    }

    public static class LinkUpdateSerializer extends JsonSerializer<LinkUpdate> {}
}
