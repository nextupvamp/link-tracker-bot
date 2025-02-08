package backend.academy.scrapper.controller;

import backend.academy.scrapper.model.Link;

import java.util.Set;

public record LinkSet(
    Set<Link> links,
    int size
) {
}
