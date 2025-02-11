package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Subscription;

public record Update(
    Subscription subscription,
    String description
) {
}
