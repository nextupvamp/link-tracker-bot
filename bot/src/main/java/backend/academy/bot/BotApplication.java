package backend.academy.bot;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.service.Command;
import backend.academy.bot.service.MessageHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({BotConfig.class})
public class BotApplication {
    private static final Logger LOG = LoggerFactory.getLogger("Bot");

    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }

    @Bean
    public ScrapperClient scrapperClient(BotConfig botConfig) {
        return new ScrapperClient(botConfig.scrapperUrl());
    }

    @Bean
    public TelegramBot bot(BotConfig botConfig, MessageHandler messageHandler) {
        TelegramBot bot = new TelegramBot(botConfig.telegramToken());
        uploadCommands(botConfig.telegramToken());
        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> {
                if (update.message() != null) {
                    long id = update.message().chat().id();
                    var reply = messageHandler.handle(
                            update.message().chat().id(), update.message().text());
                    bot.execute(new SendMessage(id, reply));
                    LOG.atInfo()
                            .setMessage("message_sent")
                            .addKeyValue(String.valueOf(id), reply)
                            .log();
                }
            });
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        return bot;
    }

    private void uploadCommands(String token) {
        List<BotCommandDto> commandDtos = new ArrayList<>();
        Arrays.stream(Command.values()).forEach(command -> {
            if (command.command() != null) {
                commandDtos.add(new BotCommandDto(command.command(), command.description()));
            }
        });
        SetCommandsRequest request = new SetCommandsRequest(commandDtos);
        try {
            URL url = new URL("https://api.telegram.org/bot" + token + "/setMyCommands");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(conn.getOutputStream(), request);

        } catch (IOException e) {
            LOG.atInfo().setMessage("Commands didn't upload successfully").log();
            return;
        }
        LOG.atInfo().setMessage("Commands have been uploaded successfully").log();
    }

    private record BotCommandDto(String command, String description) {}

    private record SetCommandsRequest(List<BotCommandDto> commands) {}
}
