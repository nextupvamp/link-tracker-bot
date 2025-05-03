package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.service.scrapper.UpdateScrapperService;
import backend.academy.scrapper.service.sender.UpdateSendingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
@EnableScheduling
public class ScheduledNotificationService implements NotificationService {
    private static final int RATE = 60_000;

    private UpdateSendingService sendingService;
    private UpdateScrapperService scrapperService;

    @Override
    @Scheduled(fixedRate = RATE)
    public void sendUpdate() {
        log.info("Scheduled task is executing");
        var updates = scrapperService.getUpdates();
        updates.forEach(sendingService::sendUpdate);
    }
}
