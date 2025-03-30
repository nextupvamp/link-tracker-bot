package backend.academy.scrapper.service;

import backend.academy.scrapper.controller.AddLinkRequest;
import backend.academy.scrapper.controller.LinkSet;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Site;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.SubscriptionRepository;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ChatService {
    private static final Supplier<NoSuchElementException> CHAT_NOT_FOUND =
            () -> new NoSuchElementException("Chat not found");
    private static final Pattern STACKOVERFLOW_URL_PATTERN =
            Pattern.compile("https://stackoverflow.com/questions/[0-9]+/?.[^/]+?");
    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile("https://github.com/.[^/]+/.[^/]+");

    private final ChatRepository chatRepository;
    private final SubscriptionRepository subscriptionRepository;

    public void deleteChat(long id) {
        var chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);
        chatRepository.delete(chat);
    }

    public void addChat(long id) {
        chatRepository.save(new Chat(id));
    }

    public Link addLink(long id, AddLinkRequest link) {
        Chat chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);

        if (!isUrlValid(link.url())) {
            throw new IllegalArgumentException("Unsupported or invalid URL: " + link.url());
        }

        Link addedLink = new Link(link.url(), link.tags(), link.filters());
        chat.links().add(addedLink);

        var subscription = subscriptionRepository
                .findByUrl(link.url())
                .orElse(new Subscription(link.url(), getSiteType(link.url())));

        subscription.subscribers().add(chat);

        subscriptionRepository.save(subscription);

        return addedLink;
    }

    public Link deleteLink(long id, String url) {
        Chat chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);
        var link = chat.findLink(url).orElseThrow(() -> new NoSuchElementException("Link not found"));
        chat.links().remove(link);

        // to avoid modernizer warning about isPresent use
        var subscription = subscriptionRepository.findByUrl(link.url()).orElse(null);

        if (subscription != null) {
            subscription.subscribers().remove(chat);
            if (subscription.subscribers().isEmpty()) {
                subscriptionRepository.delete(subscription);
            }
        }

        return link;
    }

    public LinkSet getAllLinks(long id) {
        var links = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND).links();
        return new LinkSet(links, links.size());
    }

    private boolean isUrlValid(String url) {
        return STACKOVERFLOW_URL_PATTERN.matcher(url).matches()
                || GITHUB_URL_PATTERN.matcher(url).matches();
    }

    private Site getSiteType(String url) {
        if (url.startsWith("https://stackoverflow.com")) {
            return Site.STACKOVERFLOW;
        }
        if (url.startsWith("https://github.com")) {
            return Site.GITHUB;
        }

        throw new IllegalArgumentException("Unknown site type: " + url);
    }
}
