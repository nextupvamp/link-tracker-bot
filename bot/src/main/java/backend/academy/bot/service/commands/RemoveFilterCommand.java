package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RemoveFilterCommand implements BotCommand {
    private final CommandCommons commons;
    private final CommandCachingManager cache;
    private final ScrapperClient scrapperClient;

    @Override
    public String execute(long chatId, String[] tokens) {
        ChatData chatData;
        try {
            chatData = commons.getChatDataWithState(chatId, scrapperClient, ChatState.DEFAULT);
        } catch (Exception e) {
            return e.getMessage();
        }

        if (tokens.length != 3) {
            return "Wrong format. Try \"/remove_filter <url> <key>\"";
        }

        var link = chatData.links().stream()
                .filter(it -> it.url().equals(tokens[1]))
                .findFirst();

        String result = link.map(it -> it.filters().remove(tokens[2])).orElse(null);
        if (result == null) {
            return "Link or filter not found.";
        }

        try {
            scrapperClient.updateChat(chatData);
        } catch (Exception e) {
            return NOT_AVAILABLE;
        }

        cache.evictCache(chatId);
        return "Filter has been removed successfully.";
    }

    @Override
    public String command() {
        return "/remove_filter";
    }

    @Override
    public String description() {
        return "Removes filter from a link";
    }
}
