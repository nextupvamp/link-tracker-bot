package backend.academy.scrapper.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record LinkUpdate(String url, String topic, String username, long time, String preview, List<Long> tgChatsId) {}
