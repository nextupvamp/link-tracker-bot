package backend.academy.scrapper.client.bot;

import backend.academy.scrapper.dto.LinkUpdate;

public interface BotClient {
    void sendUpdate(LinkUpdate update);
}
