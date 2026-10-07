package com.ecommerce.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> details) {

    public static ApiError of(HttpStatus status, String message, String path, Map<String, String> details) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, details);
    }
}
