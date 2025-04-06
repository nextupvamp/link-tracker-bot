package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Component
public class ListCommand implements BotCommand {
    private final ScrapperClient scrapperClient;
    private final CommandCommons commons;

    @Override
    @Cacheable(key = "#chatId", value = CommandCachingManager.CACHE_NAME)
    public String execute(long chatId, String[] tokens) {
        try {
            commons.getChatDataWithState(chatId, scrapperClient, ChatState.DEFAULT);
        } catch (Exception e) {
            return e.getMessage();
        }

        try {
            var links = scrapperClient.getAllLinks(chatId).links();
            if (links == null || links.isEmpty()) {
                return "No links found.";
            }

            StringBuilder reply = new StringBuilder();
            reply.append("Tracked links:\n");
            links.forEach(it -> reply.append(it).append("\n"));

            return reply.toString();
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().is5xxServerError()) {
                return NOT_AVAILABLE;
            }
            return String.format(ERROR_RESPONSE_FORMAT, "get list of links");
        }
    }

    @Override
    public String command() {
        return "/list";
    }

    @Override
    public String description() {
        return "Prints the list of all tracked links";
    }
}
