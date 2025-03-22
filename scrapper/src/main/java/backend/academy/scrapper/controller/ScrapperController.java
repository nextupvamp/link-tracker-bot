package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.LinkSet;
import backend.academy.scrapper.dto.RemoveLinkRequest;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.chat.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class ScrapperController {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ChatService chatService;

    @PostMapping("tg-chat/{id}")
    public void newChat(@PathVariable("id") long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }

        log.atInfo().addKeyValue("request id", id).log();

        chatService.addChat(id);
    }

    @DeleteMapping("tg-chat/{id}")
    public void deleteChat(@PathVariable("id") long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }

        log.atInfo().addKeyValue("request id", id).log();

        chatService.deleteChat(id);
    }

    @GetMapping("links")
    public LinkSet getAllLinks(@RequestParam("Tg-Chat-Id") long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }

        log.atInfo().addKeyValue("request id", id).log();

        var response = chatService.getAllLinks(id);

        log.atInfo().addKeyValue("response", response).log();

        return response;
    }

    @PostMapping("links")
    @SneakyThrows
    public Link addLink(@RequestParam("Tg-Chat-Id") long id, @RequestBody AddLinkRequest link) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }

        log.atInfo()
                .addKeyValue("request id", id)
                .addKeyValue("request body", MAPPER.writeValueAsString(link))
                .log();

        var response = chatService.addLink(id, link);

        log.atInfo().addKeyValue("response", response).log();

        return response;
    }

    @DeleteMapping("links")
    @SneakyThrows
    public Link deleteLink(@RequestParam("Tg-Chat-Id") long id, @RequestBody RemoveLinkRequest link) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }

        log.atInfo()
                .addKeyValue("request id", id)
                .addKeyValue("request body", MAPPER.writeValueAsString(link))
                .log();

        var response = chatService.deleteLink(id, link.url());

        log.atInfo().addKeyValue("response", response).log();

        return response;
    }
}
