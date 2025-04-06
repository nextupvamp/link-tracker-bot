package backend.academy.scrapper.repository.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.ChatState;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.chat.ChatJdbcRepository;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@JdbcTest
@Import({ChatJdbcRepository.class, SubscriptionJdbcRepository.class})
@Testcontainers
@TestPropertySource(properties = "app.access-type=jdbc")
public class SubscriptionJdbcRepositoryTest {
    private static final String URL = "url";
    private static final Site SITE = Site.GITHUB;

    @Autowired
    private SubscriptionJdbcRepository repository;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        WebClient.builder().build();
    }

    @Test
    @DirtiesContext
    public void testSaveAndFindById() {
        Subscription expected = new Subscription(URL, SITE);

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

        expected.subscribers().add(subscriber1);
        expected.subscribers().add(subscriber2);

        repository.save(expected);

        assertThat(expected).isEqualTo(repository.findById(URL).orElseThrow());
    }

    @Test
    @DirtiesContext
    public void testFindAll() {
        Subscription expected1 = new Subscription(URL, SITE);

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

        expected1.subscribers().add(subscriber1);
        expected1.subscribers().add(subscriber2);

        Subscription expected2 = new Subscription(URL + 1, SITE);

        expected2.subscribers().add(subscriber1);
        expected2.subscribers().add(subscriber2);

        repository.save(expected1);
        repository.save(expected2);

        var result = repository.findAll(PageRequest.of(0, 2));

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(expected1, expected2);
    }

    @Test
    @DirtiesContext
    public void testDelete() {
        Subscription expected = new Subscription(URL, SITE);

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

        expected.subscribers().add(subscriber1);
        expected.subscribers().add(subscriber2);
    }
}
