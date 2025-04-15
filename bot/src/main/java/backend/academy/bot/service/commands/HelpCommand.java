package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Component
public class HelpCommand implements BotCommand {
    private final ScrapperClient scrapperClient;

    @Override
    public String execute(long chatId, String[] tokens) {
        ChatData chatData;
        try {
            chatData = scrapperClient.getChatData(chatId);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return NOT_STARTED;
            } else {
                return NOT_AVAILABLE;
            }
        }

        if (chatData.state() != ChatState.DEFAULT) {
            return NOT_APPLICABLE;
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
