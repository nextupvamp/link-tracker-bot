package backend.academy.bot.config.bot;

import backend.academy.bot.service.MessageHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class BotConfig {
    @Bean
    public TelegramBot bot(BotConfigProperties botConfigProperties, MessageHandler messageHandler) {
        TelegramBot bot = new TelegramBot(botConfigProperties.telegramToken());

        bot.setUpdatesListener(updates -> {
            updates.forEach(update -> handleUpdate(messageHandler, update, bot));

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        bot.execute(new SetMyCommands(getCommands(messageHandler)));

        return bot;
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
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

    public BotCommand[] getCommands(MessageHandler messageHandler) {
        return messageHandler.commands().stream()
                .map(it -> new BotCommand(it.command(), it.description()))
                .toArray(BotCommand[]::new);
    }
}
