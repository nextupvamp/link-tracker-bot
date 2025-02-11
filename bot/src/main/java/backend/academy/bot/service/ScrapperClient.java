package backend.academy.bot.service;

import backend.academy.bot.model.Link;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

public class ScrapperClient {
    private final WebClient webClient;

    public ScrapperClient(String scrapperUrl) {
        webClient = WebClient.create(scrapperUrl);
    }

    public void addChat(long chatId) {
        webClient
            .post()
            .uri("/tg-chat/" + chatId)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    public void deleteChat(long chatId) {
        webClient
            .delete()
            .uri("/tg-chat/" + chatId)
            .retrieve()
            .bodyToMono(String.class)
            .map(Optional::of)
            .onErrorReturn(Optional.empty())
            .block();
    }

    public LinkSet getAllLinks(long chatId) {
        return webClient
            .get()
            .uri("/links?Tg-Chat-Id=" + chatId)
            .retrieve()
            .bodyToMono(LinkSet.class)
            .block();
    }

    public Link addLink(long chatId, Link link) {
        return webClient
            .post()
            .uri("/links?Tg-Chat-Id=" + chatId)
            .body(BodyInserters.fromValue(link))
            .retrieve()
            .bodyToMono(Link.class)
            .block();
    }

    public Optional<Link> removeLink(long chatId, Link link) {
        return webClient
            .method(HttpMethod.DELETE)
            .uri("/links?Tg-Chat-Id=" + chatId)
            .body(BodyInserters.fromValue(link))
            .retrieve()
            .bodyToMono(Link.class)
            .map(Optional::of)
            .onErrorReturn(Optional.empty())
            .block();
    }
}
