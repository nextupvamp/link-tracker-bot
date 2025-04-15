package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.model.Chat;
import java.util.Optional;

public interface ChatRepository {
    Optional<Chat> findById(long id);

    Chat save(Chat chat);

    void delete(Chat chat);
}
