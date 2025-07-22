package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.exception.MessageForUserException;
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
            chatData = commons.getChatDataWithoutState(chatId, scrapperClient, ChatState.DEFAULT);
        } catch (MessageForUserException e) {
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
