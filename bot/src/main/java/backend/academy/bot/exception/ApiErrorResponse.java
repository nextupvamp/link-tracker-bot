package backend.academy.bot.exception;

import lombok.Builder;

@Builder
public record ApiErrorResponse(
    String description,
    int code,
    String exceptionName,
    String exceptionMessage,
    StackTraceElement[] stackTrace
) {
}
