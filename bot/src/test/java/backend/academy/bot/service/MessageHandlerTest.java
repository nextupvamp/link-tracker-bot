package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

import backend.academy.bot.dto.LinkSet;
import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatState;
import backend.academy.bot.model.ChatStateData;
import backend.academy.bot.model.Link;
import backend.academy.bot.repository.ChatStateRepository;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MessageHandlerTest {
    private static final String LF = "\n";

    @Mock
    private ChatStateRepository repo;

    @Mock
    private ScrapperClient scrapper;

    @InjectMocks
    private MessageHandler messageHandler;

    @ParameterizedTest
    @ValueSource(strings = {"command", "", "/starts", "/", "/startt"})
    public void testWrongCommands(String command) {
        doReturn(Optional.of(new ChatStateData())).when(repo).findById(anyLong());
        String reply = messageHandler.handle(0L, command);
        Assertions.assertTrue(reply.startsWith("Unknown command"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/start", "/track", "/track url", "/untrack", "/help"})
    public void testCorrectCommands(String command) {
        doReturn(Optional.of(new ChatStateData())).when(repo).findById(anyLong());
        String reply = messageHandler.handle(0L, command);
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testCancelCommandEnteringFilters() { // command requires non-default state
        var state = new ChatStateData();
        state.chatState(ChatState.ENTERING_FILTERS);
        doReturn(Optional.of(state)).when(repo).findById(anyLong());
        String reply = messageHandler.handle(0L, "/cancel");
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testCancelCommandEnteringTags() { // command requires non-default state
        var state = new ChatStateData();
        state.chatState(ChatState.ENTERING_TAGS);
        doReturn(Optional.of(state)).when(repo).findById(anyLong());
        String reply = messageHandler.handle(0L, "/cancel");
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testUntrackUrlCommand() { // command requires scrapper response
        doReturn(Optional.of(new ChatStateData())).when(repo).findById(anyLong());
        doReturn(new Link("url")).when(scrapper).removeLink(anyLong(), any(Link.class));
        String reply = messageHandler.handle(0L, "/untrack url");
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testListCommand() { // command requires scrapper response
        doReturn(Optional.of(new ChatStateData())).when(repo).findById(anyLong());
        doReturn(new LinkSet(new HashSet<>(), 0)).when(scrapper).getAllLinks(anyLong());
        String reply = messageHandler.handle(0L, "/list");
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testFullList() {
        var link = new Link("url");
        var links = Set.of(link);
        doReturn(Optional.of(new ChatStateData())).when(repo).findById(anyLong());
        doReturn(new LinkSet(links, 1)).when(scrapper).getAllLinks(anyLong());
        String reply = messageHandler.handle(0L, "/list");
        assertTrue(reply.contains("url"));
    }

    @Test
    public void testEmptyList() {
        doReturn(Optional.of(new ChatStateData())).when(repo).findById(anyLong());
        doReturn(new LinkSet(new HashSet<>(), 0)).when(scrapper).getAllLinks(anyLong());
        String reply = messageHandler.handle(0L, "/list");
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testListFormat() {
        var link1 = new Link("url");

        var tags = new LinkedHashSet<String>();
        tags.add("oleg");
        tags.add("t");
        var filters = new LinkedHashSet<String>();
        filters.add("oleg:t");
        filters.add("tea:pot");
        var link2 = new Link("belt", tags, filters);

        var links = new LinkedHashSet<Link>();
        links.add(link1);
        links.add(link2);

        doReturn(Optional.of(new ChatStateData())).when(repo).findById(anyLong());
        doReturn(new LinkSet(links, 1)).when(scrapper).getAllLinks(anyLong());
        String reply = messageHandler.handle(0L, "/list");
        assertEquals(
                "Tracked links:" + LF
                        + "URL: url" + LF
                        + "Tags:" + LF
                        + "    no tags" + LF
                        + "Filters:" + LF
                        + "    no filters" + LF
                        + LF
                        + "URL: belt" + LF
                        + "Tags:" + LF
                        + "    oleg" + LF
                        + "    t" + LF
                        + "Filters:" + LF
                        + "    oleg:t" + LF
                        + "    tea:pot" + LF
                        + LF,
                reply);
    }
}
