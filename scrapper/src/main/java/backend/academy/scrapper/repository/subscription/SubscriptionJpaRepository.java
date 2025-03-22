package backend.academy.scrapper.repository.subscription;

import backend.academy.scrapper.model.Subscription;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;

@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public interface SubscriptionJpaRepository extends JpaRepository<Subscription, String>, SubscriptionRepository {}
