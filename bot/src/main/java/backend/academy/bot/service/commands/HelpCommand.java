package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class HelpCommand implements BotCommand {
    private final ScrapperClient scrapperClient;
    private final CommandCommons commons;

    @Override
    public String execute(long chatId, String[] tokens) {
        try {
            commons.getChatDataWithState(chatId, scrapperClient, ChatState.DEFAULT);
        } catch (Exception e) {
            return e.getMessage();
        }

        return "There's some help for you.";
    }

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public String description() {
        return "Prints user manual";
    }
}
