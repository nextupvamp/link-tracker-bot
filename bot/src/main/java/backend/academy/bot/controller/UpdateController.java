package backend.academy.bot.controller;

import backend.academy.bot.config.resilience.ResilienceConfig;
import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.UpdateSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
public class UpdateController {
    private final ObjectMapper mapper;
    private final UpdateSender updateSender;

    @PostMapping("updates")
    @SneakyThrows
    @RateLimiter(name = ResilienceConfig.RATE_LIMITER_NAME)
    public void postUpdates(@RequestBody LinkUpdate linkUpdate) {
        log.atInfo()
                .addKeyValue("request", mapper.writeValueAsString(linkUpdate))
                .log();
        updateSender.sendUpdates(linkUpdate);
    }
}
