package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.repository.ChatStateRepository;

public interface BotCommand {
    String execute(
            long chatId, String[] tokens, ChatStateRepository chatStateRepository, ScrapperClient scrapperClient);

    String command();

    String description();
}
