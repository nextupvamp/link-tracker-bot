package backend.academy.scrapper.controller;

import java.util.Set;

public record AddLinkRequest(String url, Set<String> tags, Set<String> filters) {}
