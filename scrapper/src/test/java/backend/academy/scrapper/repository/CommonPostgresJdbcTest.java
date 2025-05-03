package backend.academy.scrapper.repository;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.repository.chat.ChatJdbcRepository;
import backend.academy.scrapper.repository.subscription.SubscriptionJdbcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@JdbcTest
@Testcontainers
@DirtiesContext
@Import({ChatJdbcRepository.class, SubscriptionJdbcRepository.class, TestcontainersConfiguration.class})
@TestPropertySource(properties = "app.access-type=jdbc")
public class CommonPostgresJdbcTest {
    @Autowired
    protected SubscriptionJdbcRepository subscriptionRepository;

    @Autowired
    protected ChatJdbcRepository chatRepository;
}
