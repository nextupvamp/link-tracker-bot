package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.repository.ChatStateRepository;
import java.util.EnumSet;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageHandler {
    private static final EnumSet<Command> COMMANDS = EnumSet.allOf(Command.class);

    private final ChatStateRepository chatStateRepository;
    private final ScrapperClient scrapperClient;

    public String handle(long chatId, String text) {
        String[] tokens = text.split(" ");
        for (var command : COMMANDS) {
            if (tokens[0].equals(command.command())) {
                return command.execute(chatId, tokens, chatStateRepository, scrapperClient);
            }
        }
        return Command.TRACK_STAGE.execute(chatId, tokens, chatStateRepository, scrapperClient);
    }
}
