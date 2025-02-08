package backend.academy.bot.repository;

import backend.academy.bot.model.ChatData;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ChatDataRepository {
    private final Map<Long, ChatData> chats = new HashMap<>();

    public Optional<ChatData> findById(long id) {
        return Optional.ofNullable(chats.get(id));
    }

    public ChatData save(long chatId, ChatData chatData) {
        chats.put(chatId, chatData);
        return chatData;
    }
}
