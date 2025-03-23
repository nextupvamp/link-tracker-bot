package backend.academy.bot.service;

import backend.academy.bot.client.ScrapperClient;
import backend.academy.bot.model.ChatData;
import backend.academy.bot.model.ChatState;
import backend.academy.bot.model.Link;
import java.util.Collections;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum Command implements BotCommand {
    START {
        @Override
        public String execute(long chatId, String[] tokens, ScrapperClient scrapperClient) {

            ChatData newChat;
            try {
                newChat = scrapperClient.getChatData(chatId);
            } catch (ResponseStatusException e) { // if 404
                try {
                    scrapperClient.addChat(chatId);
                    return "Hello! You can see the bot's commands by entering " + HELP.command();
                } catch (Exception ex) {
                    return Commons.NOT_AVAILABLE;
                }
            }

            if (newChat.state() != ChatState.DEFAULT) {
                return Commons.NOT_APPLICABLE;
            } else {
                return "You've already started! Enter " + HELP.command() + " to see bot's commands.";
            }
        }

        @Override
        public String command() {
            return "/start";
        }

        @Override
        public String description() {
            return "Starts the bot";
        }
    },

    HELP {
        @Override
        public String execute(long chatId, String[] tokens, ScrapperClient scrapperClient) {
            ChatData chatData;
            try {
                chatData = scrapperClient.getChatData(chatId);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return Commons.NOT_STARTED;
                } else {
                    return Commons.NOT_AVAILABLE;
                }
            }

            if (chatData.state() != ChatState.DEFAULT) {
                return Commons.NOT_APPLICABLE;
            }

            return "There's some help for you.";
        }

        @Override
        public String command() {
            return "/help";
        }

        @Override
        public String description() {
            return "Prints user manual";
        }
    },

    TRACK {
        @Override
        public String execute(long chatId, String[] tokens, ScrapperClient scrapperClient) {
            ChatData chatData;
            try {
                chatData = scrapperClient.getChatData(chatId);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return Commons.NOT_STARTED;
                } else {
                    return Commons.NOT_AVAILABLE;
                }
            }

            if (chatData.state() != ChatState.DEFAULT) {
                return Commons.NOT_APPLICABLE;
            }

            if (tokens.length != 2) {
                return "Wrong format. Try \"" + TRACK.command() + " <url>\"";
            }

            chatData = new ChatData(chatId, ChatState.ENTERING_TAGS, new Link(tokens[1]), null);

            try {
                scrapperClient.updateChat(chatData);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return Commons.NOT_STARTED;
                } else {
                    return Commons.NOT_AVAILABLE;
                }
            }

            return "Link " + tokens[1] + " has been added.\n" + "You can add tags or finish adding with "
                    + CANCEL.command();
        }

        @Override
        public String command() {
            return "/track";
        }

        @Override
        public String description() {
            return "Starts tracking updates on the link";
        }
    },

    // This command works with plain text.
    // It recognizes user's stage and if
    // he isn't at entering tags or filters
    // stage his text will be treated
    // as unknown command. Otherwise, his text
    // will be treated as tags or filters
    TRACK_STAGE {
        @Override
        public String execute(long chatId, String[] tokens, ScrapperClient scrapperClient) {
            ChatData chatData;
            try {
                chatData = scrapperClient.getChatData(chatId);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return Commons.NOT_STARTED;
                } else {
                    return Commons.NOT_AVAILABLE;
                }
            }

            if (chatData.state() == ChatState.DEFAULT) {
                return Commons.UNKNOWN_COMMAND;
            }

            return switch (chatData.state()) {
                case DEFAULT -> throw new IllegalStateException("Unexpected chat state: " + chatData.state());
                case ENTERING_TAGS -> {
                    var currentLink = chatData.currentEditedLink();
                    Set<String> tags = currentLink.tags();
                    Collections.addAll(tags, tokens);

                    StringBuilder reply = new StringBuilder();
                    reply.append("Added tags:\n");
                    tags.forEach(it -> reply.append(it).append('\n'));

                    chatData = new ChatData(chatId, ChatState.ENTERING_FILTERS, currentLink, chatData.links());

                    try {
                        scrapperClient.updateChat(chatData);
                    } catch (ResponseStatusException e) {
                        yield Commons.NOT_AVAILABLE;
                    }

                    reply.append("You can add filters or finish adding with ").append(CANCEL.command());
                    yield reply.toString();
                }
                case ENTERING_FILTERS -> {
                    var currentLink = chatData.currentEditedLink();
                    Set<String> filters = currentLink.filters();

                    StringBuilder reply = new StringBuilder();
                    reply.append("Added filters:\n");
                    for (String token : tokens) {
                        if (token.contains(":")) {
                            filters.add(token);
                            reply.append(token).append("\n");
                        } else {
                            yield "Wrong format \"" + token + "\". Try \"<key1>:<value1> <key2>:<value2>...\"";
                        }
                    }

                    reply.append(Commons.finishAdding(chatId, chatData, scrapperClient));
                    yield reply.toString();
                }
            };
        }

        @Override
        public String command() {
            return null;
        }

        @Override
        public String description() {
            return null;
        }
    },

    UNTRACK {
        @Override
        public String execute(long chatId, String[] tokens, ScrapperClient scrapperClient) {
            ChatData chatData;
            try {
                chatData = scrapperClient.getChatData(chatId);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return Commons.NOT_STARTED;
                } else {
                    return Commons.NOT_AVAILABLE;
                }
            }

            if (chatData.state() != ChatState.DEFAULT) {
                return Commons.NOT_APPLICABLE;
            }

            if (tokens.length != 2) {
                return "Wrong format. Try \"" + UNTRACK.command() + " <url>\".";
            }

            try {
                scrapperClient.removeLink(chatId, new Link(tokens[1]));
                return "Link " + tokens[1] + " has been removed.";
            } catch (ResponseStatusException e) {
                var httpStatusCode = e.getStatusCode();
                var status = HttpStatus.resolve(httpStatusCode.value());
                // consider that if we can't get status then the service isn't available
                if (httpStatusCode.is5xxServerError() || status == null) {
                    return Commons.NOT_AVAILABLE;
                }
                return switch (status) {
                    case HttpStatus.BAD_REQUEST -> String.format(Commons.ERROR_RESPONSE_FORMAT, "untrack link");
                    case HttpStatus.NOT_FOUND -> "Link not found. Try " + LIST.command()
                            + " to see your actual tracked links.";
                    default -> throw new IllegalStateException("Unexpected value: " + status);
                };
            } catch (Exception e) {
                return Commons.NOT_AVAILABLE;
            }
        }

        @Override
        public String command() {
            return "/untrack";
        }

        @Override
        public String description() {
            return "Stops tracking updates on the link";
        }
    },

    CANCEL {
        @Override
        public String execute(long chatId, String[] tokens, ScrapperClient scrapperClient) {
            ChatData chatData;
            try {
                chatData = scrapperClient.getChatData(chatId);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return Commons.NOT_STARTED;
                } else {
                    return Commons.NOT_AVAILABLE;
                }
            }

            if (chatData.state() == ChatState.DEFAULT) {
                return Commons.NOT_APPLICABLE;
            }

            return Commons.finishAdding(chatId, chatData, scrapperClient);
        }

        @Override
        public String command() {
            return "/cancel";
        }

        @Override
        public String description() {
            return "Stops link adding process";
        }
    },

    LIST {
        @Override
        public String execute(long chatId, String[] tokens, ScrapperClient scrapperClient) {
            ChatData chatData;
            try {
                chatData = scrapperClient.getChatData(chatId);
            } catch (ResponseStatusException e) {
                if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return Commons.NOT_STARTED;
                } else {
                    return Commons.NOT_AVAILABLE;
                }
            }

            if (chatData.state() != ChatState.DEFAULT) {
                return Commons.NOT_APPLICABLE;
            }

            try {
                var links = scrapperClient.getAllLinks(chatId).links();
                if (links == null || links.isEmpty()) {
                    return "No links found. Try " + TRACK.command() + " to add new tracked links.";
                }

                StringBuilder reply = new StringBuilder();
                reply.append("Tracked links:\n");
                links.forEach(it -> reply.append(it).append("\n"));

                return reply.toString();
            } catch (ResponseStatusException e) {
                if (e.getStatusCode().is5xxServerError()) {
                    return Commons.NOT_AVAILABLE;
                }
                return String.format(Commons.ERROR_RESPONSE_FORMAT, "get list of links");
            }
        }

        @Override
        public String command() {
            return "/list";
        }

        @Override
        public String description() {
            return "Prints the list of all tracked links";
        }
    };

    static class Commons {
        static final String UNKNOWN_COMMAND = "Unknown command. Try " + HELP.command() + " too se actual commands.";
        private static final String ERROR_RESPONSE_FORMAT = "An error occurred while trying to %s. Try again later.";
        private static final String NOT_AVAILABLE = "Service is not available. Try again later";
        private static final String NOT_APPLICABLE = "The command is not applicable on this stage";
        private static final String NOT_STARTED = "Bot is not started";

        private static String finishAdding(long chatId, ChatData chatData, ScrapperClient scrapperClient) {
            try {
                scrapperClient.addLink(chatId, chatData.currentEditedLink());
                chatData = new ChatData(chatId, ChatState.DEFAULT, null, chatData.links());
                scrapperClient.updateChat(chatData);
                return "You've successfully finished adding.";
            } catch (ResponseStatusException e) {
                if (e.getStatusCode().is5xxServerError()) {
                    return NOT_AVAILABLE;
                }
                return String.format(ERROR_RESPONSE_FORMAT, "add new link (unsupported or invalid link)");
            }
        }
    }
}
