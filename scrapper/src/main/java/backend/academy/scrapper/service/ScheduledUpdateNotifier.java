package backend.academy.scrapper.service;

import backend.academy.scrapper.client.BotClient;
import backend.academy.scrapper.client.GitHubCheckUpdateClient;
import backend.academy.scrapper.client.StackOverflowCheckUpdateClient;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.repository.SubscriptionRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ScheduledUpdateNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledUpdateNotifier.class);
    private static final long RATE = 60_000;

    private final GitHubCheckUpdateClient gitHubClient;
    private final StackOverflowCheckUpdateClient stackOverflowClient;
    private final SubscriptionRepository subscriptionRepository;
    private final BotClient botClient;

    @Scheduled(fixedRate = RATE)
    public void sendUpdates() {
        LOG.info("scheduled task is executing");
        var updates = getUpdates();
        updates.forEach(botClient::sendUpdate);
    }

    List<LinkUpdate> getUpdates() {
        var updates = new ArrayList<LinkUpdate>();

        var subscriptions = subscriptionRepository.findAll();
        for (var subscription : subscriptions) {
            var update =
                    switch (subscription.site()) {
                            // to avoid modernizer warning about isPresent use
                        case GITHUB -> gitHubClient.checkUpdates(subscription).orElse(null);
                        case STACKOVERFLOW -> stackOverflowClient
                                .checkUpdates(subscription)
                                .orElse(null);
                    };

            if (update != null) {
                var currentSubscription = update.subscription();
                currentSubscription.update();
                subscriptionRepository.save(currentSubscription);
                List<Long> subscribersId =
                        currentSubscription.subscribers().stream().map(Chat::id).toList();
                updates.add(new LinkUpdate(0, currentSubscription.url(), update.description(), subscribersId));
            }
        }

        return updates;
    }
}
