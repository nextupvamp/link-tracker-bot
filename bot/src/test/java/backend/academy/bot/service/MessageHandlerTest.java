package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.config.RedisTestConfiguration;
import backend.academy.bot.dto.LinkSet;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import backend.academy.bot.model.Link;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(RedisTestConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = {"app.enable-kafla=false"})
public class MessageHandlerTest {
    private static final String LF = "\n";

    @MockitoBean
    private ScrapperClient scrapper;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    public void setUp() {
        var caches = cacheManager.getCacheNames();
        for (var cacheName : caches) {
            cacheManager.getCache(cacheName).clear();
        }
    }

    @Test
    public void testListIsCached() {
        var link = new Link("url");
        var links = new LinkedHashSet<Link>();
        links.add(link);

        doReturn(new ChatData(0L, ChatState.DEFAULT, null, null)).when(scrapper).getChatData(anyLong());
        doReturn(new LinkSet(links, 1)).when(scrapper).getAllLinks(anyLong());
        messageHandler.handle(0L, "/list");
        messageHandler.handle(0L, "/list");

        verify(scrapper, times(1)).getAllLinks(eq(0L));
    }

    @Test
    public void testIsCacheEvicted() {
        var link = new Link("url");
        var links = new LinkedHashSet<Link>();
        links.add(link);

        doReturn(new ChatData(0L, ChatState.DEFAULT, null, links))
                .when(scrapper)
                .getChatData(anyLong());
        doReturn(new LinkSet(links, 1)).when(scrapper).getAllLinks(anyLong());
        messageHandler.handle(0L, "/list");

        messageHandler.handle(0L, "/add_tag url tag");

        messageHandler.handle(0L, "/list");

        verify(scrapper, times(2)).getAllLinks(eq(0L));
    }

    @ParameterizedTest
    @ValueSource(strings = {"command", "", "/starts", "/", "/startt"})
    public void testWrongCommands(String command) {
        doReturn(new ChatData(0L, ChatState.DEFAULT, null, null)).when(scrapper).getChatData(anyLong());
        String reply = messageHandler.handle(0L, command);
        Assertions.assertTrue(reply.startsWith("Command not found"));
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
        var filters = new HashMap<String, String>();
        filters.put("oleg", "t");
        filters.put("tea", "pot");
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
                        + "    tea = pot" + LF
                        + "    oleg = t" + LF
                        + LF,
                reply);
    }
}
