package backend.academy.scrapper.dto;

import java.util.Map;
import java.util.Set;

public record LightChatData(Long id, Set<String> tags, Map<String, String> filters) {}
