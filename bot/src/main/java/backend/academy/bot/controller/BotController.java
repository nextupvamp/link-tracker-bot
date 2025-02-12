package backend.academy.bot.controller;

import backend.academy.bot.service.UpdateSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class BotController {
    private static final Logger LOG = LoggerFactory.getLogger(BotController.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final UpdateSender updateSender;

    @PostMapping("updates")
    @SneakyThrows
    public void postUpdates(@RequestBody LinkUpdate linkUpdate) {
        LOG.atInfo().addKeyValue("request", MAPPER.writeValueAsString(linkUpdate)).log();
        updateSender.sendUpdates(linkUpdate);
    }
}
