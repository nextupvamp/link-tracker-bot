package backend.academy.scrapper.repository.chat;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.ChatState;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.CommonPostgresJdbcTest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ChatJdbcRepositoryTest extends CommonPostgresJdbcTest {
    private static final Chat CHAT;

    static {
        CHAT = new Chat(123L, ChatState.DEFAULT);
        Link currentEditedLink = new Link("url", Set.of("tag1", "tag2"), Map.of("filter1", "filter2"));
        CHAT.currentEditedLink(currentEditedLink);
        CHAT.links().add(currentEditedLink);
        CHAT.links().add(new Link("link"));
    }

    @Test
    public void testSaveAndFindById() {
        chatRepository.save(CHAT);

        assertThat(CHAT).isEqualTo(chatRepository.findById(CHAT.id()).orElseThrow());
    }

    @Test
    public void testDelete() {
        chatRepository.save(CHAT);
        assertThat(CHAT).isEqualTo(chatRepository.findById(CHAT.id()).orElseThrow());

        chatRepository.delete(CHAT);

        assertThat(Optional.empty()).isEqualTo(chatRepository.findById(CHAT.id()));
    }
}
