package backend.academy.bot.controller;

import backend.academy.bot.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@AllArgsConstructor
public class BotControllerAdvice {

    private final ObjectMapper mapper;

    @ExceptionHandler({
        IllegalArgumentException.class,
        HttpMessageNotReadableException.class,
        HttpMediaTypeNotSupportedException.class,
        HttpRequestMethodNotSupportedException.class,
    })
    @SneakyThrows
    public ResponseEntity<?> handleIllegalArgumentException(Exception e) {
        var response = ApiErrorResponse.builder()
                .description("Incorrect request method or parameters")
                .code(400)
                .exceptionName("Bad Request")
                .exceptionMessage(e.getMessage())
                .stackTrace(e.getStackTrace())
                .build();

        log.atInfo().addKeyValue("error", mapper.writeValueAsString(response)).log();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({RequestNotPermitted.class})
    @SneakyThrows
    public ResponseEntity<?> handleTooManyRequests(Exception e) {
        var response = ApiErrorResponse.builder()
                .description("Too many requests")
                .code(429)
                .exceptionName("Too many requests")
                .exceptionMessage(e.getMessage())
                .stackTrace(e.getStackTrace())
                .build();

        log.atInfo().addKeyValue("error", mapper.writeValueAsString(response)).log();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}
