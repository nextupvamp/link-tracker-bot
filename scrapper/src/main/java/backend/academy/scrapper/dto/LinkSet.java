package backend.academy.scrapper.dto;

import java.util.Set;

public record LinkSet(Set<LinkData> links, int size) {}
