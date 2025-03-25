package backend.academy.bot.service;

import backend.academy.bot.service.commands.BotCommand;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MessageHandler {
    @Autowired(required = false)
    @Getter
    private List<BotCommand> commands;

    @Qualifier("plainTextCommand")
    @Autowired(required = false)
    private BotCommand plainTextCommand;

    public String handle(long chatId, String text) {
        if (commands == null) {
            return "No commands specified";
        }

        String[] tokens = text.split(" ");
        for (var command : commands) {
            if (tokens[0].equals(command.command())) {
                return command.execute(chatId, tokens);
            }
        }

        if (plainTextCommand != null) {
            return plainTextCommand.execute(chatId, tokens);
        }

        return "Cannot handle this command";
    }
}
