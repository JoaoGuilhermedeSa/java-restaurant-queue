package com.restaurant.queue.dto;

import java.time.LocalDateTime;

public record WebSocketMessage<T>(
        String type,
        T payload,
        LocalDateTime timestamp
) {
    public static <T> WebSocketMessage<T> of(String type, T payload) {
        return new WebSocketMessage<>(type, payload, LocalDateTime.now());
    }
}
