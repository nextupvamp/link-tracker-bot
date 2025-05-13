package backend.academy.scrapper.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import backend.academy.scrapper.config.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.service.notification.ScheduledNotificationService;
import backend.academy.scrapper.service.scrapper.UpdateScrapperService;
import backend.academy.scrapper.service.sender.HttpUpdateSendingService;
import backend.academy.scrapper.service.sender.KafkaUpdateSenderService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = {ScheduledNotificationService.class})
@EnableConfigurationProperties(ScrapperConfigProperties.class)
@TestPropertySource(properties = {"app.main-transport=http"})
public class ScheduledNotificationServiceTest {
    @MockitoBean
    private KafkaUpdateSenderService kafkaUpdateSenderService;

    @MockitoBean
    private HttpUpdateSendingService httpUpdateSendingService;

    @MockitoBean
    private UpdateScrapperService scrapperService;

    @Autowired
    private ScheduledNotificationService scheduledNotificationService;

    @Test
    public void testTransportTransition() {
        doReturn(List.of(new LinkUpdate("url", "topic", "username", 123L, "preview", null)))
                .when(scrapperService)
                .getUpdates();
        doThrow(RuntimeException.class).when(httpUpdateSendingService).sendUpdate(any());

        scheduledNotificationService.sendUpdate();

        verify(kafkaUpdateSenderService).sendUpdate(any());
    }
}
