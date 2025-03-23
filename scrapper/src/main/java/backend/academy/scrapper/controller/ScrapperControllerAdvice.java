package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.NoSuchElementException;
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
public class ScrapperControllerAdvice {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ExceptionHandler(NoSuchElementException.class)
    @SneakyThrows
    public ResponseEntity<?> handleNoSuchElementException(NoSuchElementException e) {
        var response = ApiErrorResponse.builder()
                .description("Resource not found")
                .code(404)
                .exceptionName("Not found")
                .exceptionMessage(e.getMessage())
                .stackTrace(e.getStackTrace())
                .build();

        log.atInfo().addKeyValue("error", MAPPER.writeValueAsString(response)).log();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

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

        log.atInfo().addKeyValue("error", MAPPER.writeValueAsString(response)).log();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
