package backend.academy.scrapper.service.scrapper;

import backend.academy.scrapper.config.ScrapperConfigProperties;
import backend.academy.scrapper.dto.LightChatData;
import backend.academy.scrapper.dto.LinkUpdate;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.subscription.SubscriptionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class UpdateScrapperServiceImpl implements UpdateScrapperService {
    private final ScrapperConfigProperties config;
    private final UpdateCheckersChain updateCheckersChain;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public List<LinkUpdate> getUpdates() {
        var updates = new ConcurrentLinkedDeque<LinkUpdate>();

        int page = 0;
        List<Subscription> subscriptions;
        do {
            subscriptions = subscriptionRepository
                    .findAll(PageRequest.of(page++, config.pageSize()))
                    .toList();

            subscriptions.stream().parallel().forEach((subscription) -> {
                var update = updateCheckersChain.checkUpdates(subscription);

                if (update != null) {
                    var currentSubscription = update.subscription();
                    currentSubscription.update();
                    subscriptionRepository.save(currentSubscription);

                    updates.add(LinkUpdate.builder()
                            .preview(formatPreview(update.preview(), config.previewSize()))
                            .time(update.time())
                            .topic(update.topic())
                            .url(update.subscription().url())
                            .username(update.username())
                            .chats(getChatList(subscription))
                            .build());
                }
            });
        } while (!subscriptions.isEmpty());

        return new ArrayList<>(updates);
    }

    private String formatPreview(String source, int size) {
        if (source.length() > size) {
            return source.substring(0, size) + "...";
        }
        return source;
    }

    private List<LightChatData> getChatList(Subscription subscription) {
        List<LightChatData> list = new ArrayList<>();
        String url = subscription.url();

        for (var chat : subscription.subscribers()) {
            var chatLink = chat.findLink(url).orElse(new Link());
            list.add(new LightChatData(chat.id(), chatLink.tags(), chatLink.filters()));
        }

        return list;
    }
}
