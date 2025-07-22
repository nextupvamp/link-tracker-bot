package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.config.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.service.scrapper.UpdateScrapperService;
import backend.academy.scrapper.service.sender.HttpUpdateSendingService;
import backend.academy.scrapper.service.sender.KafkaUpdateSenderService;
import backend.academy.scrapper.service.sender.UpdateSendingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableScheduling
public class ScheduledNotificationService implements NotificationService {

    private static final int RATE = 60_000;

    private UpdateSendingService mainSendingService;
    private UpdateSendingService alternativeSendingService;
    private UpdateScrapperService scrapperService;

    public ScheduledNotificationService(
            HttpUpdateSendingService httpUpdateSendingService,
            KafkaUpdateSenderService kafkaUpdateSenderService,
            UpdateScrapperService scrapperService,
            ScrapperConfigProperties configProperties) {
        this.scrapperService = scrapperService;
        if ("http".equals(configProperties.mainTransport())) {
            mainSendingService = httpUpdateSendingService;
            alternativeSendingService = kafkaUpdateSenderService;
        } else if ("kafka".equals(configProperties.mainTransport())) {
            mainSendingService = kafkaUpdateSenderService;
            alternativeSendingService = kafkaUpdateSenderService;
        }
    }

    @Override
    @Scheduled(fixedRate = RATE)
    public void sendUpdate() {
        log.info("Scheduled task is executing");
        var updates = scrapperService.getUpdates();
        for (var update : updates) {
            try {
                mainSendingService.sendUpdate(update);
            } catch (Exception e) {
                log.atError().log(e.getMessage());
                alternativeSendingService.sendUpdate(update);
            }
        }
    }
}
