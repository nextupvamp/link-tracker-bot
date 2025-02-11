package backend.academy.bot.controller;

import backend.academy.bot.service.UpdateSender;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class BotController {
    private final UpdateSender updateSender;

    @PostMapping("updates")
    public void postUpdates(@RequestBody LinkUpdate linkUpdate) {
        updateSender.sendUpdates(linkUpdate);
    }
}
