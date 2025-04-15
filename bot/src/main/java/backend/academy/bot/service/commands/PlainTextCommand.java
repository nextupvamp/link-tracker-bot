package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import java.util.Collections;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@Component
public class PlainTextCommand implements BotCommand {
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

        if (chatData.state() == ChatState.DEFAULT) {
            return UNKNOWN_COMMAND;
        }

        return switch (chatData.state()) {
            case DEFAULT -> throw new IllegalStateException("Unexpected chat state: " + chatData.state());
            case ENTERING_TAGS -> {
                var currentLink = chatData.currentEditedLink();
                Set<String> tags = currentLink.tags();
                Collections.addAll(tags, tokens);

                StringBuilder reply = new StringBuilder();
                reply.append("Added tags:\n");
                tags.forEach(it -> reply.append(it).append('\n'));

                chatData = new ChatData(chatId, ChatState.ENTERING_FILTERS, currentLink, chatData.links());

                try {
                    scrapperClient.updateChat(chatData);
                } catch (ResponseStatusException e) {
                    yield NOT_AVAILABLE;
                }

                reply.append("You can add filters or finish adding with /cancel");
                yield reply.toString();
            }
            case ENTERING_FILTERS -> {
                var currentLink = chatData.currentEditedLink();
                Set<String> filters = currentLink.filters();

                StringBuilder reply = new StringBuilder();
                reply.append("Added filters:\n");
                for (String token : tokens) {
                    if (token.contains(":")) {
                        filters.add(token);
                        reply.append(token).append("\n");
                    } else {
                        yield "Wrong format \"" + token + "\". Try \"<key1>:<value1> <key2>:<value2>...\"";
                    }
                }

                chatData.links().remove(currentLink);
                chatData.links().add(currentLink);

                reply.append(BotCommand.finishAdding(chatId, chatData, scrapperClient));
                yield reply.toString();
            }
        };
    }

    @Override
    public String command() {
        return null;
    }

    @Override
    public String description() {
        return null;
    }
}
