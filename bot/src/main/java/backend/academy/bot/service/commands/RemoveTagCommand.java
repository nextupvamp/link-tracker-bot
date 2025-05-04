package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RemoveTagCommand implements BotCommand {
    private final ScrapperClient scrapperClient;
    private final CommandCommons commons;
    private final CommandCachingManager cache;

    @Override
    public String execute(long chatId, String[] tokens) {
        ChatData chatData;
        try {
            chatData = commons.getChatDataWithState(chatId, scrapperClient, ChatState.DEFAULT);
        } catch (Exception e) {
            return e.getMessage();
        }

        if (tokens.length != 3) {
            return "Wrong format. Try \"/remove_tag <url> <tag>\"";
        }

        var link = chatData.links().stream()
                .filter(it -> it.url().equals(tokens[1]))
                .findFirst();

        Boolean result = link.map(it -> it.tags().remove(tokens[2])).orElse(null);
        if (result == null) {
            return "Link not found.";
        }

        if (result) {
            try {
                scrapperClient.updateChat(chatData);
            } catch (Exception e) {
                return NOT_AVAILABLE;
            }
            cache.evictCache(chatId);
            return "Tag has been removed successfully.";
        }

        return "Link doesn't have that tag.";
    }

    @Override
    public String command() {
        return "/remove_tag";
    }

    @Override
    public String description() {
        return "Removes a tag from a link. Format: /remove_tag <url> <tag>";
    }
}
