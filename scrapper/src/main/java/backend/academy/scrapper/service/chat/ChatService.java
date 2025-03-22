package backend.academy.scrapper.service.chat;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkSet;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.model.Site;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public interface ChatService {
    void deleteChat(long id);

    void addChat(long id);

    Link addLink(long id, AddLinkRequest link);

    Link deleteLink(long id, String url);

    LinkSet getAllLinks(long id);

    Supplier<NoSuchElementException> CHAT_NOT_FOUND = () -> new NoSuchElementException("Chat not found");
    Pattern STACKOVERFLOW_URL_PATTERN = Pattern.compile("https://stackoverflow.com/questions/[0-9]+/?.[^/]+?");
    Pattern GITHUB_URL_PATTERN = Pattern.compile("https://github.com/.[^/]+/.[^/]+");

    static boolean isUrlValid(String url) {
        return STACKOVERFLOW_URL_PATTERN.matcher(url).matches()
                || GITHUB_URL_PATTERN.matcher(url).matches();
    }

    static Site getSiteType(String url) {
        if (url.startsWith("https://stackoverflow.com")) {
            return Site.STACKOVERFLOW;
        }
        if (url.startsWith("https://github.com")) {
            return Site.GITHUB;
        }

        throw new IllegalArgumentException("Unknown site type: " + url);
    }
}
