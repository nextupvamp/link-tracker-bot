package backend.academy.bot.repository;

import backend.academy.bot.model.ChatStateData;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ChatStateRepository {
    private final Map<Long, ChatStateData> chats = new HashMap<>();

    public Optional<ChatStateData> findById(long id) {
        return Optional.ofNullable(chats.get(id));
    }

    public ChatStateData save(long chatId, ChatStateData chatStateData) {
        chats.put(chatId, chatStateData);
        return chatStateData;
    }
}
