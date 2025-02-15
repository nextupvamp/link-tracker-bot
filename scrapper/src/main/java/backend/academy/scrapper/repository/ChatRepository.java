package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Chat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepository {
    private final Map<Long, Chat> chats = new HashMap<>();

    public Optional<Chat> findById(long id) {
        return Optional.ofNullable(chats.get(id));
    }

    public void save(Chat chat) {
        chats.put(chat.id(), chat);
    }

    public void delete(Chat chat) {
        chats.remove(chat.id());
    }
}
