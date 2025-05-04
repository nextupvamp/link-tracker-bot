package backend.academy.bot.config.bot;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record BotConfigProperties(
        @NotEmpty String telegramToken,
        @NotEmpty String scrapperUrl,
        @NotEmpty String setCommandsUrlFormat,
        @NotEmpty String tgChatPath,
        @NotEmpty String linkPath) {}
