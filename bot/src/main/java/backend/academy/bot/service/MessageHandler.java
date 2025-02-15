package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatState;
import backend.academy.bot.model.ChatStateData;
import backend.academy.bot.model.Link;
import backend.academy.bot.repository.ChatStateRepository;
import java.util.Collections;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class MessageHandler {
    private static final String ERROR_RESPONSE_FORMAT = "An error occurred while trying to %s. Try again later.";
    private static final String NOT_AVAILABLE_MESSAGE = "Service is not available. Try again later";
    private static final String HELP_COMMAND = "/help";
    private static final String TRACK_COMMAND = "/track";
    private static final String UNTRACK_COMMAND = "/untrack";
    private static final String LIST_COMMAND = "/list";
    private static final String START_COMMAND = "/start";
    private static final String CANCEL_COMMAND = "/cancel";

    private final ChatStateRepository chatStateRepository;
    private final ScrapperClient scrapperClient;

    public String handle(long chatId, String text) {
        String[] tokens = text.split(" ");
        String command = tokens[0].toLowerCase();

        if (command.equals(START_COMMAND)) {
            return handleStart(chatId);
        }

        // to avoid modernizer warning about isPresent use
        var chatData = chatStateRepository.findById(chatId).orElse(null);

        if (chatData != null && chatData.chatState() == ChatState.DEFAULT) {
            return switch (command) {
                    // TODO MAKE HELP FILE
                case HELP_COMMAND -> "There's some help for you.";
                case TRACK_COMMAND -> handleTrack(chatData, tokens);
                case UNTRACK_COMMAND -> handleUntrack(chatId, tokens);
                case LIST_COMMAND -> handleList(chatId);
                default -> "Unknown command. Enter " + HELP_COMMAND + " to see actual commands.";
            };
        } else if (chatData != null) { // not default state
            if (command.equals(CANCEL_COMMAND)) {
                return finishAdding(chatId, chatData);
            }
            return switch (chatData.chatState()) {
                case ENTERING_TAGS -> handleTags(chatData, tokens);
                case ENTERING_FILTERS -> handleFilters(chatId, chatData, tokens);
                default -> throw new IllegalStateException("Unexpected state: " + chatData);
            };
        } else {
            return "Bot isn't started. Enter " + START_COMMAND;
        }
    }

    private String handleStart(long chatId) {
        var newChat = chatStateRepository.findById(chatId);

        if (newChat.isPresent()) {
            return "You've already started! Enter " + HELP_COMMAND + " to see bot's commands.";
        }

        try {
            scrapperClient.addChat(chatId);
            chatStateRepository.save(chatId, new ChatStateData());
            return "Hello! You can see the bot's commands by entering " + HELP_COMMAND;
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().is5xxServerError()) {
                return NOT_AVAILABLE_MESSAGE;
            }
            return String.format(ERROR_RESPONSE_FORMAT, "create new chat");
        }
    }

    private String handleTags(ChatStateData chatStateData, String[] tokens) {
        var currentLink = chatStateData.currentEditedLink();
        Set<String> tags = currentLink.tags();
        Collections.addAll(tags, tokens);

        StringBuilder reply = new StringBuilder();
        reply.append("Added tags:\n");
        tags.forEach(it -> reply.append(it).append('\n'));

        chatStateData.chatState(ChatState.ENTERING_FILTERS);
        reply.append("You can add filters or finish adding with " + CANCEL_COMMAND);
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
            return "Wrong format. Try \"" + TRACK_COMMAND + " <url>\"";
        }

        chatStateData.currentEditedLink(new Link(tokens[1]));
        chatStateData.chatState(ChatState.ENTERING_TAGS);

        return "Link " + tokens[1] + " has been added.\n" + "You can add tags or finish adding with " + CANCEL_COMMAND;
    }

    private String handleUntrack(long chatId, String[] tokens) {
        if (tokens.length != 2) {
            return "Wrong format. Try \"" + UNTRACK_COMMAND + " <url>\".";
        }

        try {
            var removedLink = scrapperClient.removeLink(chatId, new Link(tokens[1]));
            return "Link " + removedLink.url() + " has been removed.";
        } catch (ResponseStatusException e) {
            var httpStatusCode = e.getStatusCode();
            var status = HttpStatus.resolve(httpStatusCode.value());
            // consider that if we can't get status then the service isn't available
            if (httpStatusCode.is5xxServerError() || status == null) {
                return NOT_AVAILABLE_MESSAGE;
            }
            return switch (status) {
                case HttpStatus.BAD_REQUEST -> String.format(ERROR_RESPONSE_FORMAT, "untrack link");
                case HttpStatus.NOT_FOUND -> "Link not found. Try " + LIST_COMMAND
                        + " to see your actual tracked links.";
                default -> throw new IllegalStateException("Unexpected value: " + status);
            };
        }
    }

    private String finishAdding(long chatId, ChatStateData chatStateData) {
        try {
            scrapperClient.addLink(chatId, chatStateData.currentEditedLink());
            return "You've successfully finished adding.";
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().is5xxServerError()) {
                return NOT_AVAILABLE_MESSAGE;
            }
            return String.format(ERROR_RESPONSE_FORMAT, "add new link (unsupported or invalid link)");
        } finally {
            chatStateData.chatState(ChatState.DEFAULT);
            chatStateData.currentEditedLink(null);
        }
    }

    private String handleList(long chatId) {

        try {
            var links = scrapperClient.getAllLinks(chatId).links();
            if (links == null || links.isEmpty()) {
                return "No links found. Try " + TRACK_COMMAND + " to add new tracked links.";
            }

            StringBuilder reply = new StringBuilder();
            reply.append("Tracked links:\n");
            links.forEach(it -> reply.append(it).append("\n"));

            return reply.toString();
        } catch (ResponseStatusException e) {
            if (e.getStatusCode().is5xxServerError()) {
                return NOT_AVAILABLE_MESSAGE;
            }
            return String.format(ERROR_RESPONSE_FORMAT, "get list of links");
        }
    }
}
