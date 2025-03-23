package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.dto.LinkSet;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import backend.academy.bot.model.Link;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private ScrapperClient scrapper;

    @InjectMocks
    private MessageHandler messageHandler;

    @ParameterizedTest
    @ValueSource(strings = {"command", "", "/starts", "/", "/startt"})
    public void testWrongCommands(String command) {
        doReturn(new ChatData(0L, ChatState.DEFAULT, null, null)).when(scrapper).getChatData(anyLong());
        String reply = messageHandler.handle(0L, command);
        Assertions.assertTrue(reply.startsWith("Unknown command"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/start", "/track", "/track url", "/untrack", "/help"})
    public void testCorrectCommands(String command) {
        doReturn(new ChatData(0L, ChatState.DEFAULT, null, null)).when(scrapper).getChatData(anyLong());
        String reply = messageHandler.handle(0L, command);
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testCancelCommandEnteringFilters() { // command requires non-default state
        var state = new ChatData(0L, ChatState.ENTERING_FILTERS, null, null);
        doReturn(state).when(scrapper).getChatData(anyLong());
        String reply = messageHandler.handle(0L, "/cancel");
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testCancelCommandEnteringTags() { // command requires non-default state
        var state = new ChatData(0L, ChatState.ENTERING_TAGS, null, null);
        doReturn(state).when(scrapper).getChatData(anyLong());
        String reply = messageHandler.handle(0L, "/cancel");
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testUntrackUrlCommand() { // command requires scrapper response
        doReturn(new ChatData(0L, ChatState.DEFAULT, null, null)).when(scrapper).getChatData(anyLong());
        String reply = messageHandler.handle(0L, "/untrack url");
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testListCommand() { // command requires scrapper response
        doReturn(new ChatData(0L, ChatState.DEFAULT, null, null)).when(scrapper).getChatData(anyLong());
        doReturn(new LinkSet(new HashSet<>(), 0)).when(scrapper).getAllLinks(anyLong());
        String reply = messageHandler.handle(0L, "/list");
        assertFalse(reply.startsWith("Unknown command"));
    }

    @Test
    public void testFullList() {
        var link = new Link("url");
        var links = Set.of(link);
        doReturn(new ChatData(0L, ChatState.DEFAULT, null, null)).when(scrapper).getChatData(anyLong());
        doReturn(new LinkSet(links, 1)).when(scrapper).getAllLinks(anyLong());
        String reply = messageHandler.handle(0L, "/list");
        assertTrue(reply.contains("url"));
    }

    @Test
    public void testEmptyList() {
        doReturn(new ChatData(0L, ChatState.DEFAULT, null, null)).when(scrapper).getChatData(anyLong());
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
        var link2 = new Link(null, "belt", tags, filters);

        var links = new LinkedHashSet<Link>();
        links.add(link1);
        links.add(link2);

        doReturn(new ChatData(0L, ChatState.DEFAULT, null, null)).when(scrapper).getChatData(anyLong());
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
