package backend.academy.scrapper.dto;

import lombok.Builder;

@Builder
public record ApiErrorResponse(
        String description, int code, String exceptionName, String exceptionMessage, StackTraceElement[] stackTrace) {}
