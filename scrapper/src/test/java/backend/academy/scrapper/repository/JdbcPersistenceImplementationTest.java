package backend.academy.scrapper.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.scrapper.repository.chat.ChatJdbcRepository;
import backend.academy.scrapper.repository.chat.ChatJpaRepository;
import backend.academy.scrapper.repository.subscription.SubscriptionJdbcRepository;
import backend.academy.scrapper.repository.subscription.SubscriptionJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = "app.access-type=jdbc")
public class JdbcPersistenceImplementationTest {

    @Autowired
    private ApplicationContext applicationContext;

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
    public void testJdbcAccessType() {
        assertThat(applicationContext.getBean(SubscriptionJdbcRepository.class)).isNotNull();
        assertThat(applicationContext.getBean(ChatJdbcRepository.class)).isNotNull();
        assertThrows(
                NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(SubscriptionJpaRepository.class));
        assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(ChatJpaRepository.class));
    }
}
