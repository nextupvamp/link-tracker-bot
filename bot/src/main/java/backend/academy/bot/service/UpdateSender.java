package backend.academy.bot.service;

import backend.academy.bot.dto.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UpdateSender {

    private final TelegramBot bot;

    public void sendUpdates(LinkUpdate linkUpdate) {
        var chats = linkUpdate.chats();
        if (chats != null) {
            for (var chat : chats) {
                String userFilter = null;
                if (chat.filters() != null) {
                    userFilter = chat.filters().get("user");
                }

                if (userFilter == null || !userFilter.equals(linkUpdate.username())) {
                    bot.execute(new SendMessage(chat.id(), getMessage(linkUpdate, chat.tags())));
                }
            }
        }
    }

    private String getMessage(LinkUpdate linkUpdate, Set<String> tags) {
        StringBuilder message = new StringBuilder();

        if (tags != null && !tags.isEmpty()) {
            message.append("Tags: ");
            tags.forEach(tag -> message.append(tag).append(' '));
            message.append('\n');
        }

        message.append("New update on ").append(linkUpdate.url()).append(" :\n");
        message.append("From ")
                .append(linkUpdate.username())
                .append(" on ")
                .append(epochToString(linkUpdate.time()))
                .append('\n');
        message.append("Topic: ").append(linkUpdate.topic()).append('\n');
        message.append(linkUpdate.preview());

        return message.toString();
    }

    private String epochToString(long epochSecond) {
        Date date = new Date(epochSecond * 1000L);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        return format.format(date);
    }
}
