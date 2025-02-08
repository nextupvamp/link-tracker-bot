package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Subscription;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class SubscriptionRepository {
    private final Map<String, Subscription> subscriptions = new HashMap<>();

    public Optional<Subscription> findByUrl(String Url) {
        return Optional.ofNullable(subscriptions.get(Url));
    }

    public Subscription save(Subscription subscription) {
        subscriptions.put(subscription.url(), subscription);
        return subscription;
    }
}
