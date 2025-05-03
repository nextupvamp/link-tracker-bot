package backend.academy.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.repository.chat.ChatJdbcRepository;
import backend.academy.scrapper.repository.chat.ChatJpaRepository;
import backend.academy.scrapper.repository.subscription.SubscriptionJdbcRepository;
import backend.academy.scrapper.repository.subscription.SubscriptionJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "app.access-type=jdbc")
public class JdbcPersistenceImplementationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testJdbcAccessType() {
        assertThat(applicationContext.getBean(SubscriptionJdbcRepository.class)).isNotNull();
        assertThat(applicationContext.getBean(ChatJdbcRepository.class)).isNotNull();
        assertThrows(
                NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(SubscriptionJpaRepository.class));
        assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(ChatJpaRepository.class));
    }
}
