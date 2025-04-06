package backend.academy.scrapper.repository.chat;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.ChatState;
import backend.academy.scrapper.model.Link;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@JdbcTest
@Import(ChatJdbcRepository.class)
@Testcontainers
@TestPropertySource(properties = "app.access-type=jdbc")
public class ChatJdbcRepositoryTest {
    @Autowired
    private ChatJdbcRepository repository;

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @DirtiesContext
    public void testSaveAndFindById() {
        Chat chat = new Chat(123L, ChatState.DEFAULT);
        Link currentEditedLink = new Link("url", Set.of("tag1", "tag2"), Map.of("filter1", "filter2"));
        chat.currentEditedLink(currentEditedLink);
        chat.links().add(currentEditedLink);
        chat.links().add(new Link("link"));

        repository.save(chat);

        assertThat(chat).isEqualTo(repository.findById(chat.id()).orElseThrow());
    }

    @Test
    public void testDelete() {
        Chat chat = new Chat(123L, ChatState.DEFAULT);
        Link currentEditedLink = new Link("url", Set.of("tag1", "tag2"), Map.of("filter1", "filter2"));
        chat.currentEditedLink(currentEditedLink);
        chat.links().add(currentEditedLink);
        chat.links().add(new Link("link"));

        repository.save(chat);
        assertThat(chat).isEqualTo(repository.findById(chat.id()).orElseThrow());

        repository.delete(chat);

        assertThat(Optional.empty()).isEqualTo(repository.findById(chat.id()));
    }
}
