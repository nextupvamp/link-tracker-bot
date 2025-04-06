package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CancelCommand implements BotCommand {
    private final ScrapperClient scrapperClient;
    private final CommandCommons commons;

    @Override
    public String execute(long chatId, String[] tokens) {
        ChatData chatData;
        try {
            chatData = commons.getChatDataWithState(chatId, scrapperClient, ChatState.DEFAULT);
        } catch (Exception e) {
            return e.getMessage();
        }

        return commons.finishAdding(chatId, chatData, scrapperClient);
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
