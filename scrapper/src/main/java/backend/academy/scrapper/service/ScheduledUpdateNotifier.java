package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.repository.SubscriptionRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ScheduledUpdateNotifier {
    private static final long RATE = 60_000;

    private final GitHubClient gitHubClient;
    private final StackOverflowClient stackOverflowClient;
    private final SubscriptionRepository subscriptionRepository;
    private final BotClient botClient;

    @Scheduled(fixedRate = RATE)
    public void sendUpdates() {
        var updates = getUpdates();
        updates.forEach(botClient::sendUpdate);
    }

    private List<LinkUpdate> getUpdates() {
        var updates = new ArrayList<LinkUpdate>();

        var subscriptions = subscriptionRepository.findAll();
        for (var subscription : subscriptions) {
            var update = switch (subscription.site()) {
                case GITHUB -> gitHubClient.checkUpdates(subscription);
                case STACKOVERFLOW -> stackOverflowClient.checkUpdates(subscription);
            };

            if (update.isPresent()) {
                var unpackedUpdate = update.get();
                var unpackedSubscription = unpackedUpdate.subscription();
                List<Long> subscribersId = subscription.subscribers().stream().map(Chat::id).toList();
                updates.add(new LinkUpdate(0, unpackedSubscription.url(), unpackedUpdate.description(), subscribersId));
            }
        }

        return updates;
    }
}
