package backend.academy.bot.service;

import backend.academy.bot.model.Link;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ScrapperClient {
    private final WebClient webClient = WebClient.create("http://localhost:8081");

    public String addChat(long chatId) {
        return webClient
            .post()
            .uri("/tg-chat/" + chatId)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    public Optional<String> deleteChat(long chatId) {
        return webClient
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
