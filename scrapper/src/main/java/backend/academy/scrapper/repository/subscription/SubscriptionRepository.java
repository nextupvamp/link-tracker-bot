package backend.academy.scrapper.repository.subscription;

import backend.academy.scrapper.model.Subscription;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionRepository {
    void delete(Subscription subscription);

    Optional<Subscription> findById(String id);

    Page<Subscription> findAll(Pageable pageable);

    Subscription save(Subscription subscription);
}
