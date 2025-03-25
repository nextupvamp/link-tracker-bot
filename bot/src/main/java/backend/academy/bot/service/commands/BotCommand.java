package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import org.springframework.web.server.ResponseStatusException;

public interface BotCommand {
    String execute(long chatId, String[] tokens);

    String command();

    String description();

    String UNKNOWN_COMMAND = "Unknown command.";
    String ERROR_RESPONSE_FORMAT = "An error occurred while trying to %s. Try again later.";
    String NOT_AVAILABLE = "Service is not available. Try again later";
    String NOT_APPLICABLE = "The command is not applicable on this stage";
    String NOT_STARTED = "Bot is not started";

    static String finishAdding(long chatId, ChatData chatData, ScrapperClient scrapperClient) {
        try {
            scrapperClient.addLink(chatId, chatData.currentEditedLink());
            chatData = new ChatData(chatId, ChatState.DEFAULT, null, chatData.links());
            scrapperClient.updateChat(chatData);
            return "You've successfully finished adding.";
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().is5xxServerError()) {
                return NOT_AVAILABLE;
            }
            return String.format(ERROR_RESPONSE_FORMAT, "add new link (unsupported or invalid link)");
        }
    }
}
