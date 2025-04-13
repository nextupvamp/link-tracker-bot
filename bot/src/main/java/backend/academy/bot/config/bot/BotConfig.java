package backend.academy.bot.config.bot;

import backend.academy.bot.dto.BotCommandDto;
import backend.academy.bot.dto.SetCommandsRequest;
import backend.academy.bot.service.MessageHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@AllArgsConstructor
@Configuration
public class BotConfig {
    private final ObjectMapper objectMapper;

    @Bean
    public TelegramBot bot(BotConfigProperties botConfigProperties, MessageHandler messageHandler) {
        TelegramBot bot = new TelegramBot(botConfigProperties.telegramToken());

        uploadCommands(messageHandler, botConfigProperties);

        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> handleUpdate(messageHandler, update, bot));

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        return bot;
    }

    private void handleUpdate(MessageHandler messageHandler, Update update, TelegramBot bot) {
        if (update.message() != null) {
            long id = update.message().chat().id();

            var reply = messageHandler.handle(
                    update.message().chat().id(), update.message().text());

            bot.execute(new SendMessage(id, reply));

            log.atInfo()
                    .setMessage("message_sent")
                    .addKeyValue(String.valueOf(id), reply)
                    .log();
        }
    }

    private void uploadCommands(MessageHandler messageHandler, BotConfigProperties properties) {
        List<BotCommandDto> commandDtos = new ArrayList<>();

        for (var value : messageHandler.commands()) {
            if (value.command() != null) {
                commandDtos.add(new BotCommandDto(value.command(), value.description()));
            }
        }

        SetCommandsRequest request = new SetCommandsRequest(commandDtos);

        try {
            URL url = new URL(String.format(properties.setCommandsUrlFormat(), properties.telegramToken()));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            objectMapper.writeValue(conn.getOutputStream(), request);

        } catch (IOException e) {
            log.atInfo().setMessage("Commands didn't upload successfully").log();
            return;
        }
        log.atInfo().setMessage("Commands have been uploaded successfully").log();
    }
}
