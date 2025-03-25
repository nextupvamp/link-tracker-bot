package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import backend.academy.bot.model.Link;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Component
public class TrackCommand implements BotCommand {
    private final ScrapperClient scrapperClient;

    @Override
    public String execute(long chatId, String[] tokens) {
        ChatData chatData;
        try {
            chatData = scrapperClient.getChatData(chatId);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return BotCommand.NOT_STARTED;
            } else {
                return BotCommand.NOT_AVAILABLE;
            }
        }

        if (chatData.state() != ChatState.DEFAULT) {
            return BotCommand.NOT_APPLICABLE;
        }

        if (tokens.length != 2) {
            return "Wrong format. Try \"" + command() + " <url>\"";
        }

        chatData = new ChatData(chatId, ChatState.ENTERING_TAGS, new Link(tokens[1]), null);

        try {
            scrapperClient.updateChat(chatData);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return BotCommand.NOT_STARTED;
            } else {
                return BotCommand.NOT_AVAILABLE;
            }
        }

        return "Link " + tokens[1] + " has been added.\n" + "You can add tags or finish adding with /cancel";
    }

    @Override
    public String command() {
        return "/track";
    }

    @Override
    public String description() {
        return "Starts tracking updates on the link";
    }
}
