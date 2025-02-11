package backend.academy.bot.service;

import backend.academy.bot.model.Link;
import java.util.Set;

public record LinkSet(
    Set<Link> links,
    int size
) {
}
