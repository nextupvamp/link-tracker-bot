package backend.academy.bot.controller;

import java.util.List;

public record LinkUpdate(long id, String url, String description, List<Long> tgChatsId) {}
