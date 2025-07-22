package backend.academy.bot.exception;

import java.io.IOException;

/** It's a checked exception because it contains response for a user that should be sent. */
public class MessageForUserException extends IOException {
    public MessageForUserException(String message) {
        super(message);
    }
}
