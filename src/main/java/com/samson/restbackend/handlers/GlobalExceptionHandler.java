package com.samson.restbackend.handlers;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.lang.module.ResolutionException;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResolutionException.class)
    public ResponseEntity<Map<String, Object>> handleResolutionException(@NotNull ResponseStatusException exception) {

        assert exception.getReason() != null;

        Map<String, Object> body = Map.of(
                "status", exception.getStatusCode().value(),
                "error", exception.getReason(),
                "timestamp", Instant.now().toString()
        );

        return ResponseEntity.status(exception.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(@NotNull Exception exception) {
        return ResponseEntity.status(500).body(Map.of(
                "status", 500,
                "error", exception.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }
}
