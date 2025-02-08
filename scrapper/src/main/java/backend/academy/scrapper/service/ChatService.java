package backend.academy.scrapper.service;

import backend.academy.scrapper.controller.AddLinkRequest;
import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.controller.LinkSet;
import backend.academy.scrapper.repository.ChatRepository;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ChatService {
    private static final Supplier<NoSuchElementException> CHAT_NOT_FOUND =
        () -> new NoSuchElementException("Chat not found");

    private final ChatRepository chatRepository;

    public void deleteChat(long id) {
        var chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);
        chatRepository.delete(chat);
    }

    public void addChat(long id) {
        chatRepository.save(new Chat(id));
    }

    public Link addLink(long id, AddLinkRequest link) {
        Chat chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);
        Link addedLink = new Link(link.url(), link.tags(), link.filters());
        chat.links().add(addedLink);
        return addedLink;
    }

    public Link deleteLink(long id, String url) {
        Chat chat = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND);
        var link = chat.findLink(url).orElseThrow(() -> new NoSuchElementException("Link not found"));
        chat.links().remove(link);

        return link;
    }

    public LinkSet getAllLinks(long id) {
        var links = chatRepository.findById(id).orElseThrow(CHAT_NOT_FOUND).links();
        return new LinkSet(links, links.size());
    }
}
