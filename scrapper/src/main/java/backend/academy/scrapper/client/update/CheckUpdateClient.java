package backend.academy.scrapper.client.update;

import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.model.Subscription;
import java.util.Optional;

public interface CheckUpdateClient {
    Optional<Update> checkUpdates(Subscription subscription);
}
