package backend.academy.bot.client;

import backend.academy.bot.dto.LinkSet;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.Link;

public interface ScrapperClient {
    ChatData getChatData(long chatId);

    void updateChat(ChatData chatData);

    void addChat(long chatId);

    void deleteChat(long chatId);

    LinkSet getAllLinks(long chatId);

    void addLink(long chatId, Link link);

    void removeLink(long chatId, Link link);
}
