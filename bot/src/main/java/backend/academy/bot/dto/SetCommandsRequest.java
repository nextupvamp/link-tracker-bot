package backend.academy.bot.dto;

import java.util.List;

public record SetCommandsRequest(List<BotCommandDto> commands) {}
