package backend.academy.scrapper.dto;

import java.util.Set;

public record LinkData(Long id, String url, Set<String> tags, Set<String> filters) {}
