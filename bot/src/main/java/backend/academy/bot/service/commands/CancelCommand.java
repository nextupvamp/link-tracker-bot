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
public class CancelCommand implements BotCommand {
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

        if (chatData.state() == ChatState.DEFAULT) {
            return BotCommand.NOT_APPLICABLE;
        }

        return BotCommand.finishAdding(chatId, chatData, scrapperClient);
    }

    @Override
    public String command() {
        return "/cancel";
    }

    @Override
    public String description() {
        return "Stops link adding process";
    }
}
