package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;

public interface BotCommand {
    String execute(long chatId, String[] tokens, ScrapperClient scrapperClient);

    String command();

    String description();
}
