package backend.academy.scrapper.client;

import backend.academy.scrapper.model.Subscription;

public record Update(Subscription subscription, String description) {}
