package backend.academy.scrapper.service.scrapper;

import backend.academy.scrapper.client.CheckUpdateClient;
import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.model.Subscription;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor_ = {@Autowired(required = false)})
public class UpdateCheckersChain {
    private final List<CheckUpdateClient> checkers;

    public Update checkUpdates(Subscription subscription) {
        Update update = null;

        for (CheckUpdateClient checker : checkers) {
            update = checker.checkUpdates(subscription).orElse(null);

            if (update != null) {
                break;
            }
        }

        return update;
    }
}
