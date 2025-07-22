package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.exception.MessageForUserException;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AddFilterCommand implements BotCommand {

    private final CommandCommons commons;
    private final CommandCachingManager cache;
    private final ScrapperClient scrapperClient;

    @Override
    public String execute(long chatId, String[] tokens) {
        ChatData chatData;
        try {
            chatData = commons.getChatDataWithState(chatId, scrapperClient, ChatState.DEFAULT);
        } catch (MessageForUserException e) {
            return e.getMessage();
        }

        if (tokens.length != 3) {
            return "Wrong format. Try \"/add_filter <url> <key>=<value>\"";
        }

        var link = chatData.links().stream()
                .filter(it -> it.url().equals(tokens[1]))
                .findFirst();

        String[] keyValue = tokens[2].split("=", 2);
        String result =
                link.map(it -> it.filters().put(keyValue[0], keyValue[1])).orElse(null);
        if (result == null) {
            return "Link not found.";
        }

        try {
            scrapperClient.updateChat(chatData);
        } catch (Exception e) {
            return CommandCommons.NOT_AVAILABLE;
        }

        cache.evictCache(chatId);
        return "Filter has been added successfully.";
    }

    @Override
    public String command() {
        return "/add_filter";
    }

    @Override
    public String description() {
        return "Adds filter to a link";
    }
}
