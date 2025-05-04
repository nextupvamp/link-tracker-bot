package backend.academy.scrapper.service.sender;

import backend.academy.scrapper.config.kafka.KafkaConfigProperties;
import backend.academy.scrapper.dto.LinkUpdate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class KafkaUpdateSenderService implements UpdateSendingService {
    private final KafkaConfigProperties properties;
    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;

    @Override
    public void sendUpdate(LinkUpdate update) {
        kafkaTemplate.send(properties.topic(), update.url(), update);
    }
}
