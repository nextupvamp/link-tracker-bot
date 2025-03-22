package backend.academy.scrapper.service.chat;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkSet;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.subscription.SubscriptionRepository;
import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public void deleteChat(long id) {
        var chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);
        chatRepository.delete(chat);
    }

    @Override
    public void addChat(long id) {
        chatRepository.save(new Chat(id));
    }

    @Override
    public Link addLink(long id, AddLinkRequest link) {
        Chat chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);

        if (!ChatService.isUrlValid(link.url())) {
            throw new IllegalArgumentException("Unsupported or invalid URL: " + link.url());
        }

        Link addedLink = new Link(link.url(), link.tags(), link.filters());
        chat.links().add(addedLink);

        var subscription = subscriptionRepository
                .findById(link.url())
                .orElse(new Subscription(link.url(), ChatService.getSiteType(link.url())));

        subscription.subscribers().add(chat);

        subscriptionRepository.save(subscription);

        return addedLink;
    }

    @Override
    public Link deleteLink(long id, String url) {
        Chat chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);
        var link = chat.findLink(url).orElseThrow(() -> new NoSuchElementException("Link not found"));
        chat.links().remove(link);

        // to avoid modernizer warning about isPresent use
        var subscription = subscriptionRepository.findById(link.url()).orElse(null);

        if (subscription != null) {
            subscription.subscribers().remove(chat);
            if (subscription.subscribers().isEmpty()) {
                subscriptionRepository.delete(subscription);
            }
        }

        return link;
    }

    @Override
    public LinkSet getAllLinks(long id) {
        var links = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND).links();
        return new LinkSet(links, links.size());
    }
}
