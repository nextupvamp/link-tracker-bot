package backend.academy.bot.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @org.springframework.web.bind.annotation.ExceptionHandler({
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

        LOG.atInfo().addKeyValue("error", MAPPER.writeValueAsString(response)).log();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
