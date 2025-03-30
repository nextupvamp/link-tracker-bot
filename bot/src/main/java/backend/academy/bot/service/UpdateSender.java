package backend.academy.bot.service;

import backend.academy.bot.controller.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateSender {
    private final TelegramBot bot;

    public void sendUpdates(LinkUpdate linkUpdate) {
        // notify all the subscribers about update on the one url
        String message = "New update on " + linkUpdate.url() + " : " + linkUpdate.description();

        var chats = linkUpdate.tgChatsId();
        for (var id : chats) {
            bot.execute(new SendMessage(id, message));
        }
    }
}
