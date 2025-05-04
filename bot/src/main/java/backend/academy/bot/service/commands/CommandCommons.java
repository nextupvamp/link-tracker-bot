package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@AllArgsConstructor
public class CommandCommons {
    private final CommandCachingManager cache;

    public String finishAdding(long chatId, ChatData chatData, ScrapperClient scrapperClient) {
        try {
            scrapperClient.addLink(chatId, chatData.currentEditedLink());
            chatData = new ChatData(chatId, ChatState.DEFAULT, null, chatData.links());
            scrapperClient.updateChat(chatData);

            cache.evictCache(chatId);
            return "You've successfully finished adding.";
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().is5xxServerError()) {
                return BotCommand.NOT_AVAILABLE;
            }
            return String.format(BotCommand.ERROR_RESPONSE_FORMAT, "add new link (unsupported or invalid link)");
        }
    }

    public ChatData getChatDataWithState(long chatId, ScrapperClient scrapperClient, ChatState chatState)
            throws Exception {
        ChatData chatData = getChatData(chatId, scrapperClient);

        if (chatData.state() != chatState) {
            throw new Exception(BotCommand.NOT_APPLICABLE);
        }

        return chatData;
    }

    public ChatData getChatDataWithoutState(long chatId, ScrapperClient scrapperClient, ChatState chatState)
            throws Exception {
        ChatData chatData = getChatData(chatId, scrapperClient);

        if (chatData.state() == chatState) {
            throw new Exception(BotCommand.NOT_APPLICABLE);
        }

        return chatData;
    }

    public ChatData getChatData(long chatId, ScrapperClient scrapperClient) throws Exception {
        ChatData chatData;

        try {
            chatData = scrapperClient.getChatData(chatId);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new Exception(BotCommand.NOT_STARTED);
            } else {
                throw new Exception(BotCommand.NOT_AVAILABLE);
            }
        }

        return chatData;
    }
}
