package backend.academy.scrapper.controller;

import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.service.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ScrapperController {
    private final ChatService chatService;

    @PostMapping("tg-chat/{id}")
    public void newChat(@PathVariable("id") long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        chatService.addChat(id);
    }

    @DeleteMapping("tg-chat/{id}")
    public void deleteChat(@PathVariable("id") long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        chatService.deleteChat(id);
    }

    @GetMapping("links")
    public LinkSet getAllLinks(@RequestParam("Tg-Chat-Id") long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        return chatService.getAllLinks(id);
    }

    @PostMapping("links")
    public Link addLink(@RequestParam("Tg-Chat-Id") long id, @RequestBody AddLinkRequest link) {
        if (id < 0) {
            throw new IllegalArgumentException("ID cannot be negative");
        }
        return chatService.addLink(id, link);
    }

    @DeleteMapping("links")
    public Link deleteLink(@RequestParam("Tg-Chat-Id") long id, @RequestBody RemoveLinkRequest link) {
        return chatService.deleteLink(id, link.url());
    }
}
