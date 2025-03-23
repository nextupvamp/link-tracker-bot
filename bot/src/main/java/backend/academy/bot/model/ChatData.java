package backend.academy.bot.model;

import java.util.Set;

public record ChatData(Long id, ChatState state, Link currentEditedLink, Set<Link> links) {}
