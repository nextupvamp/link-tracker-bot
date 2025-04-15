package backend.academy.scrapper.service.sender;

import backend.academy.scrapper.client.BotHttpClient;
import backend.academy.scrapper.service.scrapper.UpdateScrapperService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ScheduledHttpUpdateSendingService implements UpdateSendingService {
    private static final long RATE = 60_000;

    private final BotHttpClient botHttpClient;
    private final UpdateScrapperService scrapperService;

    @Scheduled(fixedRate = RATE)
    @Override
    public void sendUpdate() {
        log.info("Scheduled task is executing");
        var updates = scrapperService.getUpdates();
        updates.forEach(botHttpClient::sendUpdate);
    }
}
