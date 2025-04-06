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
    private final CommandCommons commons;

    @Override
    public String execute(long chatId, String[] tokens) {
        try {
            commons.getChatDataWithState(chatId, scrapperClient, ChatState.DEFAULT);
        } catch (Exception e) {
            return e.getMessage();
        }

        if (tokens.length != 2) {
            return "Wrong format. Try \"" + command() + " <url>\"";
        }

        ChatData chatData = new ChatData(chatId, ChatState.ENTERING_TAGS, new Link(tokens[1]), null);

        try {
            scrapperClient.updateChat(chatData);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return NOT_STARTED;
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return "You cannot add that link: Unsupported or unavailable website.";
            } else {
                return NOT_AVAILABLE;
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
        return "Starts tracking updates on the link. Format: /track <url>";
    }
}
