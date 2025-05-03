package backend.academy.scrapper.service.sender;

import backend.academy.scrapper.dto.LinkUpdate;

public interface UpdateSendingService {
    void sendUpdate(LinkUpdate linkUpdate);
}
