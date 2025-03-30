package backend.academy.scrapper.service.chat;

import backend.academy.scrapper.dto.AddLinkRequest;
import backend.academy.scrapper.dto.ChatData;
import backend.academy.scrapper.dto.LinkSet;

public interface ChatService {
    void deleteChat(long id);

    void addChat(long id);

    void addLink(long id, AddLinkRequest link);

    void deleteLink(long id, String url);

    LinkSet getAllLinks(long id);

    ChatData getChatData(long id);

    void updateChatData(ChatData chatData);
}
