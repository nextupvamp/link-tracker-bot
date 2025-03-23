package backend.academy.bot.controller;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.UpdateSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class BotController {
    private final ObjectMapper mapper;
    private final UpdateSender updateSender;

    @PostMapping("updates")
    @SneakyThrows
    public void postUpdates(@RequestBody LinkUpdate linkUpdate) {
        log.atInfo()
                .addKeyValue("request", mapper.writeValueAsString(linkUpdate))
                .log();
        updateSender.sendUpdates(linkUpdate);
    }
}
