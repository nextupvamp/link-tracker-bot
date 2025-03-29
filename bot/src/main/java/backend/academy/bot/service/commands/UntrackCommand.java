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
public class UntrackCommand implements BotCommand {
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

        if (tokens.length != 2) {
            return "Wrong format. Try \"" + command() + " <url>\".";
        }

        try {
            scrapperClient.removeLink(chatId, new Link(tokens[1]));
            return "Link " + tokens[1] + " has been removed.";
        } catch (ResponseStatusException e) {
            var httpStatusCode = e.getStatusCode();
            var status = HttpStatus.resolve(httpStatusCode.value());
            // consider that if we can't get status then the service isn't available
            if (httpStatusCode.is5xxServerError() || status == null) {
                return NOT_AVAILABLE;
            }
            return switch (status) {
                case HttpStatus.BAD_REQUEST -> String.format(ERROR_RESPONSE_FORMAT, "untrack link");
                case HttpStatus.NOT_FOUND -> "Link not found. Try /list to see your actual tracked links.";
                default -> throw new IllegalStateException("Unexpected value: " + status);
            };
        } catch (Exception e) {
            return NOT_AVAILABLE;
        }
    }

    @Override
    public String command() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "Stops tracking updates on the link";
    }
}
