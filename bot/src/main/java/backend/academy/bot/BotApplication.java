package backend.academy.bot;

import backend.academy.bot.service.UpdateHandler;
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
@EnableScheduling
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }

    @Bean
    public TelegramBot bot(BotConfig botConfig, UpdateHandler updateHandler) {
        TelegramBot bot = new TelegramBot(botConfig.telegramToken());
        bot.setUpdatesListener(updates -> {
            updates.forEach(update ->
                bot.execute(new SendMessage(update.message().chat().id(), updateHandler.handle(update)))
            );
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        return bot;
    }
}
