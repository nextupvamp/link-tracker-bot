package backend.academy.bot.service;

import backend.academy.bot.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateSender {
    private final TelegramBot bot;

    public void sendUpdates(LinkUpdate linkUpdate) {
        // notify all the subscribers about update on the one url
        String message = "New update on " + linkUpdate.url() + " :\n"
                + "From " + linkUpdate.username() + " on " + epochToString(linkUpdate.time()) + "\n"
                + "Topic: " + linkUpdate.topic() + "\n"
                + linkUpdate.preview();

        var chats = linkUpdate.tgChatsId();
        for (var id : chats) {
            bot.execute(new SendMessage(id, message));
        }
    }

    private String epochToString(long epochSecond) {
        Date date = new Date(epochSecond * 1000L);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return format.format(date);
    }
}
