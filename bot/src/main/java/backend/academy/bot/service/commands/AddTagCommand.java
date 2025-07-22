package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.exception.MessageForUserException;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AddTagCommand implements BotCommand {

    private final ScrapperClient scrapperClient;
    private final CommandCommons commons;
    private final CommandCachingManager cache;

    @Override
    public String execute(long chatId, String[] tokens) {
        ChatData chatData;
        try {
            chatData = commons.getChatDataWithState(chatId, scrapperClient, ChatState.DEFAULT);
        } catch (MessageForUserException e) {
            return e.getMessage();
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
            } catch (Exception e) {
                return CommandCommons.NOT_AVAILABLE;
            }

            cache.evictCache(chatId);
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
