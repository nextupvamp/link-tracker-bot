package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Subscription;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class SubscriptionRepository {
    private final Map<String, Subscription> subscriptions = new HashMap<>();

    public Optional<Subscription> findByUrl(String Url) {
        return Optional.ofNullable(subscriptions.get(Url));
    }

    public List<Subscription> findAll() {
        return new ArrayList<>(subscriptions.values());
    }

    public void save(Subscription subscription) {
        subscriptions.put(subscription.url(), subscription);
    }

    public void delete(Subscription subscription) {
        subscriptions.remove(subscription.url());
    }
}
