package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatState;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;
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

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("manual.txt"))))) {
            return br.lines().collect(Collectors.joining());
        } catch (Exception e) {
            return "Nobody will help you.";
        }
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
