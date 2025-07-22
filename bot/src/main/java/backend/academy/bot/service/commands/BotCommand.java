package backend.academy.bot.service.commands;

public interface BotCommand {

    String execute(long chatId, String[] tokens);

    String command();

    String description();
}
