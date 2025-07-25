package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Component
public class StartCommand implements BotCommand {

    private final ScrapperClient scrapperClient;

    @Override
    public String execute(long chatId, String[] tokens) {

        ChatData newChat;
        try {
            newChat = scrapperClient.getChatData(chatId);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().value() == 404) {
                scrapperClient.addChat(chatId);
                return "Hello! You can see the bot's commands by entering /help";
            } else {
                return CommandCommons.NOT_AVAILABLE;
            }
        } catch (Exception e) {
            return CommandCommons.NOT_AVAILABLE;
        }

        if (newChat.state() != ChatState.DEFAULT) {
            return CommandCommons.NOT_APPLICABLE;
        } else {
            return "You've already started! Enter /help to see bot's commands.";
        }
    }

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public String description() {
        return "Starts the bot";
    }
}
