package backend.academy.scrapper.service.scrapper;

import backend.academy.scrapper.dto.LinkUpdate;
import java.util.List;

public interface UpdateScrapperService {
    List<LinkUpdate> getUpdates();
}
