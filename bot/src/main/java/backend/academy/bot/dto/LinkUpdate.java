package backend.academy.bot.dto;

import java.util.List;

public record LinkUpdate(String url, String topic, String username, long time, String preview, List<Long> tgChatsId) {}
