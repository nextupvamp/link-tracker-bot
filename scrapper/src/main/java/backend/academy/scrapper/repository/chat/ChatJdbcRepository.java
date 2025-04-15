package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.ChatState;
import backend.academy.scrapper.model.Link;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jdbc")
@Repository
@AllArgsConstructor
public class ChatJdbcRepository implements ChatRepository {
    private final ChatRowMapper CHAT_ROW_MAPPER = new ChatRowMapper();
    private final LinkRowMapper LINK_ROW_MAPPER = new LinkRowMapper();

    private final JdbcClient jdbcClient;

    @Override
    @Transactional(readOnly = true)
    public Optional<Chat> findById(long id) {
        var links = jdbcClient
                .sql("select * from link l left join chat_links cl on l.id = cl.link_id where cl.chat_id = ?")
                .param(id)
                .query(LINK_ROW_MAPPER)
                .list();

        for (var link : links) {
            fetchTagsAndFilters(link);
        }

        var chat = jdbcClient
                .sql("select * from chat where id = ?")
                .param(id)
                .query(CHAT_ROW_MAPPER)
                .optional();

        chat.ifPresent(it -> {
            it.links(new HashSet<>(links));
            var currentEditedLink = jdbcClient
                    .sql("select * from link where id = (select curr_edited_link from chat where id = ?)")
                    .param(id)
                    .query(LINK_ROW_MAPPER)
                    .optional();
            it.currentEditedLink(currentEditedLink.orElse(null));
        });

        return chat;
    }

    @Override
    @Transactional
    public Chat save(Chat chat) {
        jdbcClient
                .sql("insert into chat (id, state) values (?, ?) on conflict (id) do update set state = ?")
                .param(chat.id())
                .param(chat.state().name())
                .param(chat.state().name())
                .update();

        if (chat.currentEditedLink() != null) {
            long id = saveLink(chat, chat.currentEditedLink());
            jdbcClient
                    .sql("update chat set curr_edited_link = ? where id = ?")
                    .param(id)
                    .param(chat.id())
                    .update();
        }

        removeNotActualLinks(chat);

        for (var link : chat.links()) {
            saveLink(chat, link);
        }

        return chat;
    }

    @Override
    @Transactional
    public void delete(Chat chat) {
        jdbcClient.sql("delete from chat where id = ?").param(chat.id()).update();
    }

    private void fetchTagsAndFilters(Link link) {
        var tags =
                jdbcClient
                        .sql("select tag from link_tags where link = ?")
                        .param(link.id())
                        .query()
                        .singleColumn()
                        .stream()
                        .map(it -> (String) it)
                        .collect(Collectors.toSet());

        var filters =
                jdbcClient
                        .sql("select filter from link_filters where link = ?")
                        .param(link.id())
                        .query()
                        .singleColumn()
                        .stream()
                        .map(it -> (String) it)
                        .collect(Collectors.toSet());

        link.tags(tags);
        link.filters(filters);
    }

    private long saveLink(Chat chat, Link link) {
        var result = jdbcClient
                .sql("select id from link where url = ?")
                .param(link.url())
                .query()
                .optionalValue();

        Long linkId = result.map(it -> (Long) it).orElseGet(() ->
                (Long) jdbcClient.sql("select nextval ('link_seq')").query().singleValue());

        jdbcClient
                .sql("insert into link (id, url) values (?, ?) on conflict (id) do update set url = ?")
                .param(linkId)
                .param(link.url())
                .param(link.url())
                .update();

        jdbcClient
                .sql("insert into chat_links (chat_id, link_id) values (?, ?) on conflict do nothing")
                .param(chat.id())
                .param(linkId)
                .update();

        for (var tag : link.tags()) {
            jdbcClient
                    .sql("insert into link_tags (link, tag) values (?, ?)")
                    .param(linkId)
                    .param(tag)
                    .update();
        }

        for (var filter : link.filters()) {
            jdbcClient
                    .sql("insert into link_filters (link, filter) values (?, ?)")
                    .param(linkId)
                    .param(filter)
                    .update();
        }

        return linkId;
    }

    private void removeNotActualLinks(Chat chat) {
        var persistentLinks = new HashSet<>(jdbcClient
                .sql("select * from link l left join chat_links cl on l.id = cl.link_id where cl.chat_id = ?")
                .param(chat.id())
                .query(LINK_ROW_MAPPER)
                .list());
        var actualLinks = chat.links();

        for (var link : persistentLinks) {
            if (!actualLinks.contains(link)) {
                jdbcClient.sql("delete from link where id = ?").param(link.id()).update();
            }
        }
    }

    public static class LinkRowMapper implements RowMapper<Link> {
        @Override
        public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
            Link link = new Link();
            link.id(rs.getLong("id"));
            link.url(rs.getString("url"));
            return link;
        }
    }

    public static class ChatRowMapper implements RowMapper<Chat> {
        @Override
        public Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
            Chat chat = new Chat();
            chat.id(rs.getLong("id"));
            chat.state(ChatState.valueOf(rs.getString("state")));
            return chat;
        }
    }
}
