package backend.academy.scrapper.service.sender;

import backend.academy.scrapper.config.KafkaConfigProperties;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.service.scrapper.UpdateScrapperService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "kafka")
@AllArgsConstructor
public class ScheduledKafkaUpdateSenderService implements UpdateSendingService {
    private static final long RATE = 60_000;

    private final KafkaConfigProperties properties;
    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;
    private final UpdateScrapperService scrapperService;

    @Scheduled(fixedRate = RATE)
    @Override
    public void sendUpdate() {
        log.info("Scheduled task is executing");
        var updates = scrapperService.getUpdates();
        updates.forEach(this::sendUpdate);
    }

    private void sendUpdate(LinkUpdate update) {
        kafkaTemplate.send(properties.topic(), update.url(), update);
    }
}
