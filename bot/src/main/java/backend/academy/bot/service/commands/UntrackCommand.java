package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.exception.MessageForUserException;
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
    private final CommandCommons commons;
    private final CommandCachingManager cache;

    @Override
    public String execute(long chatId, String[] tokens) {
        try {
            commons.getChatDataWithState(chatId, scrapperClient, ChatState.DEFAULT);
        } catch (MessageForUserException e) {
            return e.getMessage();
        }

        if (tokens.length != 2) {
            return String.format("Wrong format. Try \"%s <url>\"", command());
        }

        try {
            scrapperClient.removeLink(chatId, new Link(tokens[1]));
            cache.evictCache(chatId);
            return String.format("Link %s has been removed.", tokens[1]);
        } catch (ResponseStatusException e) {
            var httpStatusCode = e.getStatusCode();
            var status = HttpStatus.resolve(httpStatusCode.value());
            if (httpStatusCode.is5xxServerError() || status == null) {
                return CommandCommons.NOT_AVAILABLE;
            }
            return switch (status) {
                case HttpStatus.BAD_REQUEST -> String.format(CommandCommons.ERROR_RESPONSE_FORMAT, "untrack link");
                case HttpStatus.NOT_FOUND -> "Link not found. Try /list to see your actual tracked links.";
                default -> throw new IllegalStateException(String.format("Unexpected value: %s", status));
            };
        } catch (Exception e) {
            return CommandCommons.NOT_AVAILABLE;
        }
    }

    @Override
    public String command() {
        return "/untrack";
    }

    @Override
    public String description() {
        return "Stops tracking updates on the link. Format: /untrack <url>";
    }
}
