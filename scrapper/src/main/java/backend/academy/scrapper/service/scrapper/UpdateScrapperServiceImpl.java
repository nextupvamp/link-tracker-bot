package backend.academy.scrapper.service.scrapper;

import backend.academy.scrapper.ScrapperConfigProperties;
import backend.academy.scrapper.client.GitHubCheckUpdateClient;
import backend.academy.scrapper.client.StackOverflowCheckUpdateClient;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.subscription.SubscriptionRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UpdateScrapperServiceImpl implements UpdateScrapperService {
    private final ScrapperConfigProperties config;
    private final GitHubCheckUpdateClient gitHubClient;
    private final StackOverflowCheckUpdateClient stackOverflowClient;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public List<LinkUpdate> getUpdates() {
        var updates = new ArrayList<LinkUpdate>();

        int page = 0;
        List<Subscription> subscriptions;
        do {
            subscriptions = subscriptionRepository
                    .findAll(PageRequest.of(page++, config.pageSize()))
                    .toList();

            subscriptions.stream().parallel().forEach((subscription) -> {
                var update =
                        switch (subscription.site()) {
                            case GITHUB -> gitHubClient
                                    .checkUpdates(subscription)
                                    .orElse(null);
                            case STACKOVERFLOW -> stackOverflowClient
                                    .checkUpdates(subscription)
                                    .orElse(null);
                        };

                if (update != null) {
                    var currentSubscription = update.subscription();
                    currentSubscription.update();
                    subscriptionRepository.save(currentSubscription);

                    List<Long> subscribersId = currentSubscription.subscribers().stream()
                            .map(Chat::id)
                            .toList();

                    updates.add(LinkUpdate.builder()
                            .preview(update.preview())
                            .time(update.time())
                            .topic(update.topic())
                            .url(update.subscription().url())
                            .username(update.username())
                            .tgChatsId(subscribersId)
                            .build());
                }
            });
        } while (!subscriptions.isEmpty());

        return updates;
    }
}
