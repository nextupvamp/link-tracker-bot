package backend.academy.scrapper.service.chat;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.ChatData;
import backend.academy.scrapper.dto.LinkData;
import backend.academy.scrapper.dto.LinkSet;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.ChatState;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Subscription;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.subscription.SubscriptionRepository;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public void deleteChat(long id) {
        var chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);
        chatRepository.delete(chat);
    }

    @Override
    public ChatData getChatData(long id) {
        Chat chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);
        var mappedCurrentEditedLink = mapLinkToLinkData(chat.currentEditedLink());
        var mappedLinks = chat.links().stream().map(this::mapLinkToLinkData).collect(Collectors.toSet());
        return new ChatData(chat.id(), chat.state(), mappedCurrentEditedLink, mappedLinks);
    }

    @Override
    @Transactional
    public void updateChatData(ChatData chatData) {
        var chat = chatRepository.findById(chatData.id()).orElseThrow(CHAT_NOT_FOUND);
        chat.state(chatData.state());
        chat.currentEditedLink(mapLinkDataToLink(chatData.currentEditedLink()));

        if (!ChatService.isUrlValid(chat.currentEditedLink().url())) {
            throw new IllegalArgumentException(
                    "Unsupported or invalid URL: " + chat.currentEditedLink().url());
        }

        if (chatData.links() != null) {
            chat.links(chatData.links().stream().map(this::mapLinkDataToLink).collect(Collectors.toSet()));

            if (chatData.currentEditedLink() != null) {
                chat.links().remove(chat.currentEditedLink()); // remove the older version of the link
                chat.links().add(chat.currentEditedLink());
            }

        } else if (chatData.currentEditedLink() != null) {
            // Set.of() causes UOE
            var links = new HashSet<Link>();
            links.add(chat.currentEditedLink());
            chat.links(links);
        }

        chatRepository.save(chat);
    }

    @Override
    public void addChat(long id) {
        chatRepository.save(new Chat(id, ChatState.DEFAULT));
    }

    @Override
    @Transactional
    public void addLink(long id, AddLinkRequest link) {
        Chat chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);

        if (!ChatService.isUrlValid(link.url())) {
            throw new IllegalArgumentException("Unsupported or invalid URL: " + link.url());
        }

        Link addedLink = new Link(link.url(), link.tags(), link.filters());
        chat.links().add(addedLink);
        chatRepository.save(chat);

        var subscription = subscriptionRepository
                .findById(link.url())
                .orElse(new Subscription(link.url(), ChatService.getSiteType(link.url())));

        subscription.subscribers().add(chat);

        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void deleteLink(long id, String url) {
        Chat chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);
        var link = chat.findLink(url).orElseThrow(() -> new NoSuchElementException("Link not found"));
        chat.links().remove(link);
        chatRepository.save(chat);
        // to avoid modernizer warning about isPresent use
        var subscription = subscriptionRepository.findById(link.url()).orElse(null);

        if (subscription != null) {
            subscription.subscribers().remove(chat);
            if (subscription.subscribers().isEmpty()) {
                subscriptionRepository.delete(subscription);
            }
        }
    }

    @Override
    public LinkSet getAllLinks(long id) {
        var links = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND).links();
        return new LinkSet(links.stream().map(this::mapLinkToLinkData).collect(Collectors.toSet()), links.size());
    }

    private LinkData mapLinkToLinkData(Link link) {
        if (link == null) return null;
        return new LinkData(link.id(), link.url(), link.tags(), link.filters());
    }

    private Link mapLinkDataToLink(LinkData linkData) {
        if (linkData == null) return null;
        return new Link(linkData.id(), linkData.url(), linkData.tags(), linkData.filters());
    }
}
