package backend.academy.scrapper.dto;

import java.util.Map;
import java.util.Set;
import lombok.Builder;

@Builder
public record LinkUpdate(
        String url, String topic, String username, long time, String preview, Map<Long, Set<String>> chats) {}
