package com.adityaverma.blood_bank_system.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

public sealed interface ApiResponse<T> permits SuccessResponse, ErrorResponse {
    boolean success();
    String message();
    LocalDateTime timestamp();
    String path();
}

public record SuccessResponse<T>(
        boolean success,
        String message,
        @JsonInclude(JsonInclude.Include.NON_NULL) T data,
        LocalDateTime timestamp,
        String path
) implements ApiResponse<T> {
    public SuccessResponse(T data, String message, String path) {
        this(true, message, data, LocalDateTime.now(), path);
    }

    public static <T> SuccessResponse<T> of(T data, String message, String path) {
        return new SuccessResponse<>(data, message, path);
    }

    public static <T> SuccessResponse<T> of(T data, String message) {
        return new SuccessResponse<>(data, message, null);
    }

    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(data, "Success", null);
    }
}

public record ErrorResponse(
        boolean success,
        String message,
        LocalDateTime timestamp,
        String path,
        String errorCode,
        @JsonInclude(JsonInclude.Include.NON_NULL) Object details
) implements ApiResponse<Void> {
    public ErrorResponse(String message, String errorCode, String path, Object details) {
        this(false, message, LocalDateTime.now(), path, errorCode, details);
    }

    public static ErrorResponse of(String message, String errorCode, String path) {
        return new ErrorResponse(message, errorCode, path, null);
    }

    public static ErrorResponse of(String message, String errorCode) {
        return new ErrorResponse(message, errorCode, null, null);
    }
}