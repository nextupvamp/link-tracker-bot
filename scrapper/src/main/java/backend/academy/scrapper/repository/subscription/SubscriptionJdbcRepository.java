package backend.academy.scrapper.repository.subscription;

import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.chat.ChatJdbcRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jdbc")
@Repository
@RequiredArgsConstructor
public class SubscriptionJdbcRepository implements SubscriptionRepository {
    private static final SubscriptionRowMapper SUBSCRIPTION_ROW_MAPPER = new SubscriptionRowMapper();

    private final ChatJdbcRepository chatRepository; // dry
    private final JdbcClient jdbcClient;

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findById(String id) {
        var subscription = jdbcClient
                .sql("select * from subscription where url = ?")
                .param(id)
                .query(SUBSCRIPTION_ROW_MAPPER)
                .optional();
        subscription.ifPresent(it -> fetchSubscribers(id, it));

        return subscription;
    }

    @Override
    @Transactional
    public Page<Subscription> findAll(Pageable pageable) {
        var subscriptions = jdbcClient
                .sql("select * from subscription offset ? limit ?")
                .param(pageable.getOffset())
                .param(pageable.getPageSize())
                .query(SUBSCRIPTION_ROW_MAPPER)
                .list();

        subscriptions.forEach(it -> fetchSubscribers(it.url(), it));

        return new PageImpl<>(subscriptions);
    }

    @Override
    @Transactional
    public Subscription save(Subscription subscription) {
        var subscribers = subscription.subscribers();
        subscribers.forEach(chatRepository::save);

        jdbcClient
                .sql(
                        "insert into subscription (updated, last_update, site, url) values (?, ?, ?, ?) on conflict (url) do update set updated = ?, last_update = ?, site = ?")
                .param(subscription.updated())
                .param(subscription.lastUpdate())
                .param(subscription.site().name())
                .param(subscription.url())
                .param(subscription.updated())
                .param(subscription.lastUpdate())
                .param(subscription.site().name())
                .update();

        subscribers.forEach(it -> jdbcClient
                .sql("insert into subscriber (chat_id, subscription) values (?, ?)")
                .param(it.id())
                .param(subscription.url())
                .update());

        return subscription;
    }

    @Override
    @Transactional
    public void delete(Subscription subscription) {
        jdbcClient
                .sql("delete from subscriber where subscription = ?")
                .param(subscription.url())
                .update();
        jdbcClient
                .sql("delete from subscription where url = ?")
                .param(subscription.url())
                .update();
    }

    private void fetchSubscribers(String id, Subscription it) {
        var subscribers =
                jdbcClient
                        .sql("select chat_id from subscriber where subscription = ?")
                        .param(id)
                        .query()
                        .singleColumn()
                        .stream()
                        .map(elem -> (long) elem)
                        .toList();

        subscribers.forEach(elem -> {
            var chat = chatRepository.findById(elem);
            chat.ifPresent(pres -> it.subscribers().add(pres));
        });
    }

    public static class SubscriptionRowMapper implements RowMapper<Subscription> {
        @Override
        public Subscription mapRow(ResultSet rs, int rowNum) throws SQLException {
            var subscription = new Subscription();
            subscription.updated(rs.getBoolean("updated"));
            subscription.lastUpdate(rs.getLong("last_update"));
            subscription.site(Site.valueOf(rs.getString("site")));
            subscription.url(rs.getString("url"));
            return subscription;
        }
    }
}
