package backend.academy.scrapper.service.sender;

import backend.academy.scrapper.client.bot.BotHttpClient;
import backend.academy.scrapper.dto.LinkUpdate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class HttpUpdateSendingService implements UpdateSendingService {

    private final BotHttpClient botHttpClient;

    @Override
    public void sendUpdate(LinkUpdate linkUpdate) {
        botHttpClient.sendUpdate(linkUpdate);
    }
}
