package backend.academy.scrapper.repository.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.ChatState;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.CommonPostgresJdbcTest;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

public class SubscriptionJdbcRepositoryTest extends CommonPostgresJdbcTest {
    private static final String URL = "url";
    private static final Site SITE = Site.GITHUB;
    private static final Subscription SUBSCRIPTION;

    static {
        SUBSCRIPTION = new Subscription(URL, SITE);

        Chat subscriber1 = new Chat(123L, ChatState.DEFAULT);
        Link currentEditedLink1 = new Link(URL, Set.of("tag1", "tag2"), Map.of("filter1", "filter2"));
        subscriber1.currentEditedLink(currentEditedLink1);
        subscriber1.links().add(currentEditedLink1);
        subscriber1.links().add(new Link("link"));

        Chat subscriber2 = new Chat(124L, ChatState.DEFAULT);
        Link currentEditedLink2 = new Link(URL + 1, Set.of("tagg1", "tagg2"), Map.of("filterr1", "filterr2"));
        subscriber2.currentEditedLink(currentEditedLink2);
        subscriber2.links().add(currentEditedLink2);
        subscriber2.links().add(new Link("linkk"));

        SUBSCRIPTION.subscribers().add(subscriber1);
        SUBSCRIPTION.subscribers().add(subscriber2);
    }

    @Test
    public void testSaveAndFindById() {
        subscriptionRepository.save(SUBSCRIPTION);

        assertThat(SUBSCRIPTION).isEqualTo(subscriptionRepository.findById(URL).orElseThrow());
    }

    @Test
    public void testFindAll() {
        Subscription subscription2 = new Subscription(URL + 1, SITE);

        subscriptionRepository.save(SUBSCRIPTION);
        subscriptionRepository.save(subscription2);

        var result = subscriptionRepository.findAll(PageRequest.of(0, 2));

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(SUBSCRIPTION, subscription2);
    }

    @Test
    public void testDelete() {
        subscriptionRepository.save(SUBSCRIPTION);
        assertThat(subscriptionRepository.findAll(PageRequest.of(0, 2))).hasSize(1);
        subscriptionRepository.delete(SUBSCRIPTION);
        assertThat(subscriptionRepository.findAll(PageRequest.of(0, 2))).hasSize(0);
    }
}
