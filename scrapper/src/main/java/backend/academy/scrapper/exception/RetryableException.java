package backend.academy.scrapper.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class RetryableException extends ResponseStatusException {
    public RetryableException(HttpStatusCode status) {
        super(status);
    }
}
