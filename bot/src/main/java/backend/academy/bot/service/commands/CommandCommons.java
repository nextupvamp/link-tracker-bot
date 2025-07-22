package backend.academy.bot.service.commands;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.exception.MessageForUserException;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@AllArgsConstructor
public class CommandCommons {

    public static final String ERROR_RESPONSE_FORMAT = "An error occurred while trying to %s. Try again.";
    public static final String NOT_AVAILABLE = "Service is not available. Try again later.";
    public static final String NOT_APPLICABLE = "The command is not applicable on this stage.";
    public static final String NOT_STARTED = "Bot is not started.";

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
                return NOT_AVAILABLE;
            }
            return String.format(ERROR_RESPONSE_FORMAT, "add new link (unsupported or invalid link)");
        } catch (Exception e) {
            return NOT_AVAILABLE;
        }
    }

    public ChatData getChatDataWithState(long chatId, ScrapperClient scrapperClient, ChatState chatState)
            throws MessageForUserException {
        ChatData chatData = getChatData(chatId, scrapperClient);

        if (chatData.state() != chatState) {
            throw new MessageForUserException(NOT_APPLICABLE);
        }

        return chatData;
    }

    public ChatData getChatDataWithoutState(long chatId, ScrapperClient scrapperClient, ChatState chatState)
            throws MessageForUserException {
        ChatData chatData = getChatData(chatId, scrapperClient);

        if (chatData.state() == chatState) {
            throw new MessageForUserException(NOT_APPLICABLE);
        }

        return chatData;
    }

    public ChatData getChatData(long chatId, ScrapperClient scrapperClient) throws MessageForUserException {
        ChatData chatData;

        try {
            chatData = scrapperClient.getChatData(chatId);
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new MessageForUserException(NOT_STARTED);
            } else {
                throw new MessageForUserException(NOT_AVAILABLE);
            }
        } catch (Exception e) {
            throw new MessageForUserException(NOT_AVAILABLE);
        }

        return chatData;
    }
}
