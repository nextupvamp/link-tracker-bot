package backend.academy.scrapper.client;

import backend.academy.scrapper.model.Subscription;

import java.util.Optional;

public interface CheckUpdateClient {
    Optional<Update> checkUpdates(Subscription subscription);
}
