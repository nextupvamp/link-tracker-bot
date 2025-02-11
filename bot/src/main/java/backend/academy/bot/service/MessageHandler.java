package backend.academy.bot.service;

import backend.academy.bot.model.ChatStateData;
import backend.academy.bot.model.ChatState;
import backend.academy.bot.model.Link;
import backend.academy.bot.repository.ChatStateRepository;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import java.net.URI;
import java.util.Collections;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageHandler {
    private final ChatStateRepository chatStateRepository;
    private final ScrapperClient scrapperClient;

    public String handle(Update update) {
        Message message = update.message();
        long chatId = message.chat().id();
        String[] tokens = message.text().split(" ");
        String command = tokens[0].toLowerCase();

        if (command.equals("/start")) {
            return handleStart(chatId);
        }

        var chatData = chatStateRepository.findById(chatId);

        if (chatData.isPresent() && chatData.get().chatState() == ChatState.DEFAULT) {
            return switch (command) {
                case "/help" -> "There's some help for you.";
                case "/track" -> handleTrack(chatData.get(), tokens);
                case "/untrack" -> handleUntrack(chatId, tokens);
                case "/list" -> handleList(chatId);
                default -> "Unknown command. Enter /help to see actual commands.";
            };
        } else if (chatData.isPresent()) {
            if (command.equals("/cancel")) {
                return finishAdding(chatId, chatData.get());
            }
            return switch (chatData.get().chatState()) {
                case ENTERING_TAGS -> handleTags(chatData.get(), tokens);
                case ENTERING_FILTERS -> handleFilters(chatId, chatData.get(), tokens);
                default -> throw new IllegalStateException("Unexpected state: " + chatData.get());
            };
        } else {
            return "Bot isn't started. Enter /start";
        }
    }

    private String handleStart(long chatId) {
        String reply;
        var newChat = chatStateRepository.findById(chatId);
        if (newChat.isEmpty()) {
            scrapperClient.addChat(chatId);
            chatStateRepository.save(chatId, new ChatStateData());
            reply = "Hello! You can see the bot's commands by entering /help";
        } else {
            reply = "You've already started! Enter /help to see bot's commands.";
        }
        return reply;
    }

    private String handleTags(ChatStateData chatStateData, String[] tokens) {
        var currentLink = chatStateData.currentEditedLink();
        Set<String> tags = currentLink.tags();
        Collections.addAll(tags, tokens);

        StringBuilder reply = new StringBuilder();
        reply.append("Added tags:\n");
        tags.forEach(it -> reply.append(it).append('\n'));

        chatStateData.chatState(ChatState.ENTERING_FILTERS);
        reply.append("You can add filters or finish adding with /cancel");
        return reply.toString();
    }

    private String handleFilters(long chatId, ChatStateData chatStateData, String[] tokens) {
        var currentLink = chatStateData.currentEditedLink();
        Set<String> filters = currentLink.filters();

        StringBuilder reply = new StringBuilder();
        reply.append("Added filters:\n");
        for (String token : tokens) {
            if (token.contains(":")) {
                filters.add(token);
                reply.append(token).append("\n");
            } else {
                return "Wrong format \"" + token + "\". Try \"<key1>:<value1> <key2>:<value2>...\"";
            }
        }

        reply.append(finishAdding(chatId, chatStateData));
        return reply.toString();
    }

    private String handleTrack(ChatStateData chatStateData, String[] tokens) {
        if (tokens.length != 2) {
            return "Wrong format. Try \"/track <url>\"";
        }
        try {
            var ignored = URI.create(tokens[1]);
        } catch (Exception e) {
            return tokens[1] + " is not a valid URL";
        }


        chatStateData.currentEditedLink(new Link(tokens[1]));
        chatStateData.chatState(ChatState.ENTERING_TAGS);

        StringBuilder reply = new StringBuilder();
        reply.append("Link ").append(tokens[1]).append(" has been added.\n");
        reply.append("You can add tags or finish adding with /cancel");

        return reply.toString();
    }

    private String handleUntrack(long chatId, String[] tokens) {
        if (tokens.length != 2) {
            return "Wrong format. Try \"/untrack <url>\".";
        }

        // there we are deleting and checking if link was found and deleted
        if (scrapperClient.removeLink(chatId, new Link(tokens[1])).isEmpty()) {
            return "Link not found. Try /list to see your actual tracked links.";
        }

        return "Link " + tokens[1] + " has been removed.";
    }

    private String finishAdding(long chatId, ChatStateData chatStateData) {
        scrapperClient.addLink(chatId, chatStateData.currentEditedLink());

        chatStateData.chatState(ChatState.DEFAULT);
        chatStateData.currentEditedLink(null);
        return "You've finished adding.";
    }

    private String handleList(long chatId) {
        var links = scrapperClient.getAllLinks(chatId).links();

        if (links == null || links.isEmpty()) {
            return "No links found. Try /track to add new tracked links.";
        }

        StringBuilder reply = new StringBuilder();
        reply.append("Tracked links:\n");
        links.forEach(it -> reply.append(it).append("\n"));

        return reply.toString();
    }
}
