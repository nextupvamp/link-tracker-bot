package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Subscription;
import java.util.Optional;

public interface Client {
    Optional<Update> checkUpdates(Subscription subscription);
}
