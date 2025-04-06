package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.scrapper.client.PingClient;
import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.ChatState;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.subscription.SubscriptionRepository;
import backend.academy.scrapper.service.chat.ChatServiceImpl;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {
    @Mock
    private ChatRepository chatRepo;

    @Mock
    private SubscriptionRepository subRepo;

    @Mock
    private PingClient pingClient;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    public void testAddLink() {
        var link = new AddLinkRequest("https://github.com/oleg/tee", Set.of("tag"), Map.of("fil", "ter"));
        var chat = new Chat(0L, ChatState.DEFAULT);
        doReturn(Optional.of(chat)).when(chatRepo).findById(anyLong());
        doReturn(true).when(pingClient).ping(anyString());
        chatService.addLink(0L, link);
        verify(chatRepo, times(1)).save(eq(chat));
        verify(subRepo, times(1)).save(any(Subscription.class));
    }

    @Test
    public void testDeleteLink() {
        var chat = new Chat(0L, ChatState.DEFAULT);
        var link = new Link("https://github.com/oleg/tee");
        chat.links().add(link);
        var subscription = new Subscription("https://github.com/oleg/tee", Site.GITHUB);
        subscription.subscribers().add(chat);

        doReturn(Optional.of(subscription)).when(subRepo).findById(anyString());
        doReturn(Optional.of(chat)).when(chatRepo).findById(anyLong());

        chatService.deleteLink(0L, "https://github.com/oleg/tee");

        assertAll(
                () -> assertTrue(subscription.subscribers().isEmpty()),
                () -> assertTrue(chat.links().isEmpty()));

        verify(chatRepo, times(1)).save(eq(chat));
        verify(subRepo, times(1)).delete(any(Subscription.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"url, https://githab.com/oleg/tee, https://stackoverflow/questons/1234"})
    public void testIncorrectLinkValidation(String url) {
        var link = new AddLinkRequest(url, Set.of("tag"), Map.of("fil", "ter"));
        doReturn(Optional.of(new Chat(0L, ChatState.DEFAULT))).when(chatRepo).findById(anyLong());
        assertThrows(IllegalArgumentException.class, () -> chatService.addLink(0L, link));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "https://github.com/vampnextup/logs-analyzer-service",
                "https://stackoverflow.com/questions/12345",
                "https://stackoverflow.com/questions/12345/something"
            })
    public void testCorrectLinkValidation(String url) {
        var link = new AddLinkRequest(url, Set.of("tag"), Map.of("fil", "ter"));
        var chat = new Chat(0L, ChatState.DEFAULT);
        doReturn(Optional.of(chat)).when(chatRepo).findById(anyLong());
        doReturn(true).when(pingClient).ping(anyString());
        chatService.addLink(0L, link);
        verify(chatRepo, times(1)).save(eq(chat));
        verify(subRepo, times(1)).save(any(Subscription.class));
    }
}
