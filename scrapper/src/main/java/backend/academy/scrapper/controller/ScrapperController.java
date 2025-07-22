package backend.academy.scrapper.controller;

import backend.academy.scrapper.config.resilience.ResilienceConfig;
import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.ChatData;
import backend.academy.scrapper.dto.LinkSet;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.service.chat.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class ScrapperController {

    private final ObjectMapper mapper;
    private final ChatService chatService;

    @GetMapping("tg-chat/{id}")
    @RateLimiter(name = ResilienceConfig.RATE_LIMITER_NAME)
    public ChatData getChatData(@Positive @PathVariable long id) {
        log.atInfo().addKeyValue("request id", id).log();

        return chatService.getChatData(id);
    }

    @PatchMapping("tg-chat")
    @SneakyThrows
    @RateLimiter(name = ResilienceConfig.RATE_LIMITER_NAME)
    public void updateChatData(@Valid @RequestBody ChatData chat) {
        log.atInfo().addKeyValue("request", mapper.writeValueAsString(chat)).log();

        chatService.updateChatData(chat);
    }

    @PostMapping("tg-chat/{id}")
    @RateLimiter(name = ResilienceConfig.RATE_LIMITER_NAME)
    public void newChat(@Positive @PathVariable("id") long id) {
        log.atInfo().addKeyValue("request id", id).log();

        chatService.addChat(id);
    }

    @DeleteMapping("tg-chat/{id}")
    @RateLimiter(name = ResilienceConfig.RATE_LIMITER_NAME)
    public void deleteChat(@Positive @PathVariable("id") long id) {
        log.atInfo().addKeyValue("request id", id).log();

        chatService.deleteChat(id);
    }

    @GetMapping("links")
    @RateLimiter(name = ResilienceConfig.RATE_LIMITER_NAME)
    public LinkSet getAllLinks(@Positive @RequestParam("Tg-Chat-Id") long id) {
        log.atInfo().addKeyValue("request id", id).log();

        var response = chatService.getAllLinks(id);

        log.atInfo().addKeyValue("response", response).log();

        return response;
    }

    @PostMapping("links")
    @SneakyThrows
    @RateLimiter(name = ResilienceConfig.RATE_LIMITER_NAME)
    public void addLink(@Positive @RequestParam("Tg-Chat-Id") long id, @RequestBody AddLinkRequest link) {
        log.atInfo()
                .addKeyValue("request id", id)
                .addKeyValue("request body", mapper.writeValueAsString(link))
                .log();

        chatService.addLink(id, link);
    }

    @DeleteMapping("links")
    @SneakyThrows
    @RateLimiter(name = ResilienceConfig.RATE_LIMITER_NAME)
    public void deleteLink(@Positive @RequestParam("Tg-Chat-Id") long id, @RequestBody RemoveLinkRequest link) {
        log.atInfo()
                .addKeyValue("request id", id)
                .addKeyValue("request body", mapper.writeValueAsString(link))
                .log();

        chatService.deleteLink(id, link.url());
    }
}
