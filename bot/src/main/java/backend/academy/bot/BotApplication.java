package backend.academy.bot;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.service.MessageHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
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
}
