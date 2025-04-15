package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@AllArgsConstructor
public class AddTagCommand implements BotCommand {
    private final ScrapperClient scrapperClient;

    @Override
    public String execute(long chatId, String[] tokens) {
        ChatData chatData;
        try {
            chatData = scrapperClient.getChatData(chatId);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return NOT_STARTED;
            } else {
                return NOT_AVAILABLE;
            }
        }

        if (tokens.length != 3) {
            return "Wrong format. Try \"/add_tag <url> <tag>\"";
        }

        var link = chatData.links().stream()
                .filter(it -> it.url().equals(tokens[1]))
                .findFirst();

        Boolean result = link.map(it -> it.tags().add(tokens[2])).orElse(null);
        if (result == null) {
            return "Link not found.";
        }

        if (result) {
            try {
                scrapperClient.updateChat(chatData);
            } catch (ResponseStatusException e) {
                return NOT_AVAILABLE;
            }
            return "Tag has been added successfully.";
        }

        return "Link already has that tag.";
    }

    @Override
    public String command() {
        return "/add_tag";
    }

    @Override
    public String description() {
        return "Adds a tag to a link. Format: /add_tag <url> <tag>";
    }
}
