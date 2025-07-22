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

        var persistedSub = subscriptionRepository.findById(URL).orElseThrow();
        assertThat(persistedSub.url()).isEqualTo(SUBSCRIPTION.url());
        assertThat(persistedSub.site()).isEqualTo(SUBSCRIPTION.site());
        assertThat(persistedSub.subscribers()).isEqualTo(SUBSCRIPTION.subscribers());
        assertThat(persistedSub.updated()).isEqualTo(SUBSCRIPTION.updated());
        assertThat(persistedSub.lastUpdate()).isEqualTo(SUBSCRIPTION.lastUpdate());
    }

    @Test
    public void testFindAll() {
        Subscription subscription2 = new Subscription(URL + 1, SITE);

        subscriptionRepository.save(SUBSCRIPTION);
        subscriptionRepository.save(subscription2);

        var result = subscriptionRepository.findAll(PageRequest.of(0, 2));

        assertThat(result).hasSize(2);
        var persistedSub1 =
                result.stream().filter(sub -> sub.url().equals(URL)).findFirst().orElseThrow();
        var persistedSub2 = result.stream()
                .filter(sub -> sub.url().equals(URL + 1))
                .findFirst()
                .orElseThrow();

        assertThat(persistedSub1.url()).isEqualTo(SUBSCRIPTION.url());
        assertThat(persistedSub1.site()).isEqualTo(SUBSCRIPTION.site());
        assertThat(persistedSub1.subscribers()).isEqualTo(SUBSCRIPTION.subscribers());
        assertThat(persistedSub1.updated()).isEqualTo(SUBSCRIPTION.updated());
        assertThat(persistedSub1.lastUpdate()).isEqualTo(SUBSCRIPTION.lastUpdate());
        assertThat(persistedSub2.url()).isEqualTo(subscription2.url());
        assertThat(persistedSub2.site()).isEqualTo(subscription2.site());
        assertThat(persistedSub2.subscribers()).isEqualTo(subscription2.subscribers());
        assertThat(persistedSub2.updated()).isEqualTo(subscription2.updated());
        assertThat(persistedSub2.lastUpdate()).isEqualTo(subscription2.lastUpdate());
    }

    @Test
    public void testDelete() {
        subscriptionRepository.save(SUBSCRIPTION);
        assertThat(subscriptionRepository.findAll(PageRequest.of(0, 2))).hasSize(1);
        subscriptionRepository.delete(SUBSCRIPTION);
        assertThat(subscriptionRepository.findAll(PageRequest.of(0, 2))).hasSize(0);
    }
}
