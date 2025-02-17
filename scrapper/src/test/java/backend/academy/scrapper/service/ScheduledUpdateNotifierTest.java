package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import backend.academy.scrapper.client.GitHubCheckUpdateClient;
import backend.academy.scrapper.client.StackOverflowCheckUpdateClient;
import backend.academy.scrapper.client.Update;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.SubscriptionRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ScheduledUpdateNotifierTest {
    @Mock
    private GitHubCheckUpdateClient gitHubClient;

    @Mock
    private StackOverflowCheckUpdateClient stackOverflowClient;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private ScheduledUpdateNotifier notifier;

    @Test
    public void testNotifyUsers() {
        var subA = new Subscription("a", Site.GITHUB);
        subA.subscribers().add(new Chat(1L));
        subA.subscribers().add(new Chat(2L));

        var subB = new Subscription("b", Site.STACKOVERFLOW);
        subB.subscribers().add(new Chat(3L));

        var subscriptions = List.of(subA, subB);

        doReturn(subscriptions).when(subscriptionRepository).findAll();
        doReturn(Optional.of(new Update(subscriptions.getFirst(), "")))
                .when(gitHubClient)
                .checkUpdates(any());
        doReturn(Optional.of(new Update(subscriptions.getLast(), "")))
                .when(stackOverflowClient)
                .checkUpdates(any());

        var updates = notifier.getUpdates();

        // test fails sometimes because of the order of elements
        // that's why I sort the list there
        var firstList = new ArrayList<>(updates.getFirst().tgChatsId());
        Collections.sort(firstList);

        assertAll(
                () -> assertEquals(List.of(1L, 2L), firstList),
                () -> assertEquals(List.of(3L), updates.getLast().tgChatsId()));
    }
}
