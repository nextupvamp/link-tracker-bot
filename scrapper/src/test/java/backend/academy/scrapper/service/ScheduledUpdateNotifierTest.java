package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import backend.academy.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.client.GitHubCheckUpdateClient;
import backend.academy.scrapper.client.StackOverflowCheckUpdateClient;
import backend.academy.scrapper.dto.LightChatData;
import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.ChatState;
import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.subscription.SubscriptionJpaRepository;
import backend.academy.scrapper.repository.subscription.SubscriptionRepository;
import backend.academy.scrapper.service.scrapper.UpdateCheckersChain;
import backend.academy.scrapper.service.scrapper.UpdateScrapperService;
import backend.academy.scrapper.service.scrapper.UpdateScrapperServiceImpl;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
        classes = {
            ScrapperConfigProperties.class,
            SubscriptionJpaRepository.class,
            GitHubCheckUpdateClient.class,
            StackOverflowCheckUpdateClient.class,
            UpdateCheckersChain.class,
            UpdateScrapperServiceImpl.class
        })
@TestPropertySource(properties = "app.access-type=jpa")
public class ScheduledUpdateNotifierTest {
    @MockitoBean
    private GitHubCheckUpdateClient gitHubClient;

    @MockitoBean
    private StackOverflowCheckUpdateClient stackOverflowClient;

    @MockitoBean
    private SubscriptionRepository subscriptionRepository;

    @MockitoBean
    private ScrapperConfigProperties config;

    @Autowired
    private UpdateScrapperService scrapper;

    @Test
    public void testNotifyUsers() {
        var subA = new Subscription("a", Site.GITHUB);
        subA.subscribers().add(new Chat(1L, ChatState.DEFAULT));
        subA.subscribers().add(new Chat(2L, ChatState.DEFAULT));

        var subB = new Subscription("b", Site.STACKOVERFLOW);
        subB.subscribers().add(new Chat(3L, ChatState.DEFAULT));

        var subscriptions = List.of(subA, subB);
        var subscriptionsPage = new PageImpl(subscriptions);

        doReturn(10).when(config).pageSize();
        doReturn(subscriptionsPage).when(subscriptionRepository).findAll(PageRequest.of(0, 10));
        doReturn(new PageImpl(List.of())).when(subscriptionRepository).findAll(PageRequest.of(1, 10));

        doReturn(Optional.of(new Update(subscriptions.getFirst(), "", "", 0L, "")))
                .when(gitHubClient)
                .checkUpdates(eq(subA));
        doReturn(Optional.of(new Update(subscriptions.getLast(), "", "", 0L, "")))
                .when(stackOverflowClient)
                .checkUpdates(eq(subB));

        var updates = scrapper.getUpdates();

        updates.sort(Comparator.comparingInt(it -> it.chats().size()));

        assertAll(
                () -> assertEquals(
                        Set.of(1L, 2L),
                        updates.getLast().chats().stream()
                                .map(LightChatData::id)
                                .collect(Collectors.toSet())),
                () -> assertEquals(
                        Set.of(3L),
                        updates.getFirst().chats().stream()
                                .map(LightChatData::id)
                                .collect(Collectors.toSet())));
    }

    @ParameterizedTest
    @CsvSource({"123456,12345...", "12345,12345", "1,1"})
    public void testPreviewFormatLongString(String preview, String expected) {
        var subscription = new Subscription("a", Site.GITHUB);
        subscription.subscribers().add(new Chat(1L, ChatState.DEFAULT));

        var subscriptions = List.of(subscription);
        var subscriptionsPage = new PageImpl(subscriptions);

        doReturn(10).when(config).pageSize();
        doReturn(subscriptionsPage).when(subscriptionRepository).findAll(PageRequest.of(0, 10));
        doReturn(new PageImpl(List.of())).when(subscriptionRepository).findAll(PageRequest.of(1, 10));

        doReturn(Optional.of(new Update(subscriptions.getFirst(), "", "", 0L, preview)))
                .when(gitHubClient)
                .checkUpdates(eq(subscription));

        doReturn(5).when(config).previewSize();

        var updates = scrapper.getUpdates();

        assertThat(expected).isEqualTo(updates.getLast().preview());
    }
}
