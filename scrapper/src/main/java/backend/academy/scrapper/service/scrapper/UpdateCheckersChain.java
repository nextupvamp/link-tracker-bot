package backend.academy.scrapper.service.scrapper;

import backend.academy.scrapper.client.CheckUpdateClient;
import backend.academy.scrapper.dto.Update;
import backend.academy.scrapper.model.Subscription;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateCheckersChain {
    @Autowired(required = false)
    private List<CheckUpdateClient> checkers;

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
