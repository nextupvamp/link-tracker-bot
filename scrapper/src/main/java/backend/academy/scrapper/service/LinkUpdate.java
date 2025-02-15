package backend.academy.scrapper.service;

import java.util.List;

public record LinkUpdate(long id, String url, String description, List<Long> tgChatsId) {}
