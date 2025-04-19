package backend.academy.bot.service;

import backend.academy.bot.service.commands.BotCommand;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class MessageHandler {
    @Getter
    private final List<BotCommand> commands;

    private final BotCommand plainTextCommand;

    public MessageHandler(List<BotCommand> commands, @Qualifier("plainTextCommand") BotCommand plainTextCommand) {
        this.commands = commands;
        this.plainTextCommand = plainTextCommand;
    }

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
