package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.model.Chat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;

@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public interface ChatJpaRepository extends JpaRepository<Chat, Long>, ChatRepository {}
