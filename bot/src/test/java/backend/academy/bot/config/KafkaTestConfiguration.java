package backend.academy.bot.config;

import backend.academy.bot.dto.LinkUpdate;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@EnableKafka
public class KafkaTestConfiguration {

    @Bean
    @ServiceConnection
    @RestartScope
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"));
    }

    @Bean
    public KafkaTemplate<String, LinkUpdate> kafkaTemplate(KafkaContainer kafka) {
        var properties = new HashMap<String, Object>();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LinkUpdateSerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(properties));
    }

    @Bean
    public KafkaTemplate<String, byte[]> byteArrayKafkaTemplate(KafkaContainer kafka) {
        var properties = new HashMap<String, Object>();

        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(properties));
    }

    @Bean
    public ConsumerFactory<String, LinkUpdate> consumerFactory(KafkaContainer kafka) {
        var properties = new HashMap<String, Object>();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        properties.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        properties.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, LinkUpdateDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "group");

        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, LinkUpdate>>
            kafkaListenerContainerFactory(ConsumerFactory<String, LinkUpdate> consumerFactory) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, LinkUpdate>();

        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setAutoStartup(true);

        return factory;
    }

    @Bean
    public ProducerFactory<String, LinkUpdate> producerFactory(KafkaContainer kafka) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public NewTopic newTopic() {
        return new NewTopic("updates-test", 1, (short) 1);
    }

    public static class LinkUpdateSerializer extends JsonSerializer<LinkUpdate> {}

    public static class LinkUpdateDeserializer extends JsonDeserializer<LinkUpdate> {}
}
