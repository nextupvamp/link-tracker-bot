package backend.academy.bot.service.commands;

public interface BotCommand {
    String execute(long chatId, String[] tokens);

    String command();

    String description();

    String ERROR_RESPONSE_FORMAT = "An error occurred while trying to %s. Try again.";
    String NOT_AVAILABLE = "Service is not available. Try again later";
    String NOT_APPLICABLE = "The command is not applicable on this stage";
    String NOT_STARTED = "Bot is not started";
}
