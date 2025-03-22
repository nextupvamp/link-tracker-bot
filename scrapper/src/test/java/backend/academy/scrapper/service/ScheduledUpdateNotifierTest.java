package backend.academy.scrapper.service;

import backend.academy.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.client.GitHubCheckUpdateClient;
import backend.academy.scrapper.client.StackOverflowCheckUpdateClient;
import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.subscription.SubscriptionRepository;
import backend.academy.scrapper.service.scrapper.UpdateScrapperService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import backend.academy.scrapper.service.scrapper.UpdateScrapperServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class ScheduledUpdateNotifierTest {
    @Mock
    private GitHubCheckUpdateClient gitHubClient;

    @Mock
    private StackOverflowCheckUpdateClient stackOverflowClient;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private ScrapperConfigProperties config;

    @InjectMocks
    private UpdateScrapperServiceImpl scrapper;
    @Test
    public void testNotifyUsers() {
        var subA = new Subscription("a", Site.GITHUB);
        subA.subscribers().add(new Chat(1L));
        subA.subscribers().add(new Chat(2L));

        var subB = new Subscription("b", Site.STACKOVERFLOW);
        subB.subscribers().add(new Chat(3L));

        var subscriptions = List.of(subA, subB);
        var subscriptionsPage = new PageImpl(subscriptions);

        doReturn(10).when(config).pageSize();
        doReturn(subscriptionsPage).when(subscriptionRepository).findAll(PageRequest.of(0, 10));
        doReturn(new PageImpl(List.of())).when(subscriptionRepository).findAll(PageRequest.of(1, 10));

        doReturn(Optional.of(new Update(subscriptions.getFirst(), "", "", 0L, "")))
                .when(gitHubClient)
                .checkUpdates(any());
        doReturn(Optional.of(new Update(subscriptions.getLast(), "", "", 0L, "")))
                .when(stackOverflowClient)
                .checkUpdates(any());

        var updates = scrapper.getUpdates();

        var firstList = new ArrayList<>(updates.getFirst().tgChatsId());
        Collections.sort(firstList);

        assertAll(
                () -> assertEquals(List.of(1L, 2L), firstList),
                () -> assertEquals(List.of(3L), updates.getLast().tgChatsId()));
    }
}
