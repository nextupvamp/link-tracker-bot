package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.subscription.SubscriptionRepository;
import backend.academy.scrapper.service.chat.ChatService;
import java.util.Optional;
import java.util.Set;
import backend.academy.scrapper.service.chat.ChatServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {
    @Mock
    private ChatRepository chatRepo;

    @Mock
    private SubscriptionRepository subRepo;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    public void testAddLink() {
        var link = new AddLinkRequest("https://github.com/oleg/tee", Set.of("tag"), Set.of("fil:ter"));
        doReturn(Optional.of(new Chat(0L))).when(chatRepo).findById(anyLong());
        var addedLink = chatService.addLink(0L, link);
        assertEquals(new Link("https://github.com/oleg/tee", Set.of("tag"), Set.of("fil:ter")), addedLink);
    }

    @Test
    public void testDeleteLink() {
        var chat = new Chat(0L);
        var link = new Link("https://github.com/oleg/tee");
        chat.links().add(link);
        var subscription = new Subscription("https://github.com/oleg/tee", Site.GITHUB);
        subscription.subscribers().add(chat);

        doReturn(Optional.of(subscription)).when(subRepo).findById(anyString());
        doReturn(Optional.of(chat)).when(chatRepo).findById(anyLong());

        //subscription.subscribers().remove(chat);
        //subscription.subscribers().isEmpty();

        var deletedLink = chatService.deleteLink(0L, "https://github.com/oleg/tee");

        assertAll(
                () -> assertEquals(link, deletedLink),
                () -> assertTrue(subscription.subscribers().isEmpty()),
                () -> assertTrue(chat.links().isEmpty()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"url, https://githab.com/oleg/tee, https://stackoverflow/questons/1234"})
    public void testIncorrectLinkValidation(String url) {
        var link = new AddLinkRequest(url, Set.of("tag"), Set.of("fil:ter"));
        doReturn(Optional.of(new Chat(0L))).when(chatRepo).findById(anyLong());
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
        var link = new AddLinkRequest(url, Set.of("tag"), Set.of("fil:ter"));
        doReturn(Optional.of(new Chat(0L))).when(chatRepo).findById(anyLong());
        assertEquals(new Link(url, Set.of("tag"), Set.of("fil:ter")), chatService.addLink(0L, link));
    }

    // everything is stored in sets or maps so there are no special logic
    // to handle that situation, the link will just be rewritten
    @Deprecated
    @Disabled
    @Test
    public void testAddExistingLink() {
        var link = new AddLinkRequest(
                "https://github.com/vampnextup/logs-analyzer-service", Set.of("tag"), Set.of("fil:ter"));
        doReturn(Optional.of(new Chat(0L))).when(chatRepo).findById(anyLong());
        chatService.addLink(0L, link);
        var newLink = new AddLinkRequest(
                "https://github.com/vampnextup/logs-analyzer-service", Set.of("oleg"), Set.of("tin:kov"));
        chatService.addLink(0L, newLink);

        assertAll(
                () -> assertTrue(chatService
                        .getAllLinks(0L)
                        .links()
                        .contains(new Link(
                                "https://github.com/vampnextup/logs-analyzer-service",
                                Set.of("oleg"),
                                Set.of("tin:kov")))),
                () -> assertEquals(
                        1, chatService.getAllLinks(0L).links().size()));
    }
}
