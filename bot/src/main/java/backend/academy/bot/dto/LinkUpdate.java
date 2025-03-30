package backend.academy.bot.dto;

import java.util.Map;
import java.util.Set;

public record LinkUpdate(
        String url, String topic, String username, long time, String preview, Map<Long, Set<String>> chats) {}
