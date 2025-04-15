package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Component
public class ListCommand implements BotCommand {
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

        if (chatData.state() != ChatState.DEFAULT) {
            return NOT_APPLICABLE;
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
