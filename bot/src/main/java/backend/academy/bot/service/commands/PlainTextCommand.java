package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class PlainTextCommand implements BotCommand {
    private final ScrapperClient scrapperClient;
    private final CommandCachingManager cachingManager;
    private final CommandCommons commons;

    @Override
    public String execute(long chatId, String[] tokens) {
        ChatData chatData;
        try {
            chatData = commons.getChatData(chatId, scrapperClient);

            if (chatData.state() == ChatState.DEFAULT) {
                return "Command not found";
            }
        } catch (Exception e) {
            return e.getMessage();
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
                } catch (Exception e) {
                    yield NOT_AVAILABLE;
                }

                cachingManager.evictCache(chatId);
                reply.append(
                        "You can add filters or finish adding with /cancel. In the current version only filtering by the user is available");
                yield reply.toString();
            }
            case ENTERING_FILTERS -> {
                var currentLink = chatData.currentEditedLink();
                Map<String, String> filters = currentLink.filters();

                StringBuilder reply = new StringBuilder();
                reply.append("Added filters:\n");
                for (String token : tokens) {
                    if (token.contains("=")) {
                        String[] keyValue = token.split("=", 2);
                        filters.put(keyValue[0], keyValue[1]);
                        reply.append(token).append("\n");
                    } else {
                        yield "Wrong format \"" + token + "\". Try \"<key1>=<value1> <key2>=<value2>...\"";
                    }
                }

                chatData.links().remove(currentLink);
                chatData.links().add(currentLink);

                reply.append(commons.finishAdding(chatId, chatData, scrapperClient));
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
