package backend.academy.bot;

import backend.academy.bot.service.ScrapperClient;
import backend.academy.bot.service.MessageHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({BotConfig.class})
public class BotApplication {
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
            updates.forEach(update ->
                bot.execute(new SendMessage(update.message().chat().id(), messageHandler.handle(update)))
            );
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        return bot;
    }
}
