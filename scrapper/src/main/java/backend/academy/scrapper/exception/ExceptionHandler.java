package backend.academy.scrapper.exception;

import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElementException(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.builder()
                        .description("Resource not found")
                        .code(404)
                        .exceptionName("Not found")
                        .exceptionMessage(e.getMessage())
                        .stackTrace(e.getStackTrace())
                        .build());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({
        IllegalArgumentException.class,
        HttpMessageNotReadableException.class,
        HttpMediaTypeNotSupportedException.class,
        HttpRequestMethodNotSupportedException.class,
    })
    public ResponseEntity<?> handleIllegalArgumentException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.builder()
                        .description("Incorrect request method or parameters")
                        .code(400)
                        .exceptionName("Bad Request")
                        .exceptionMessage(e.getMessage())
                        .stackTrace(e.getStackTrace())
                        .build());
    }
}
