package backend.academy.scrapper.dto;

import java.util.Map;
import java.util.Set;

public record LinkData(Long id, String url, Set<String> tags, Map<String, String> filters) {}
